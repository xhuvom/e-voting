/******************************************************************************
 * e-voting system                                                            *
 * Copyright (C) 2016 DSX Technologies Limited.                               *
 * *
 * This program is free software; you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation; either version 2 of the License, or          *
 * (at your option) any later version.                                        *
 * *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied                         *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * *
 * You can find copy of the GNU General Public License in LICENSE.txt file    *
 * at the top-level directory of this distribution.                           *
 * *
 * Removal or modification of this copyright notice is prohibited.            *
 * *
 ******************************************************************************/

package uk.dsxt.voting.common.cryptoVote;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.eclipse.jetty.util.ArrayQueue;
import uk.dsxt.voting.common.demo.NetworkConnectorDemo;
import uk.dsxt.voting.common.domain.dataModel.NodeVoteReceipt;
import uk.dsxt.voting.common.domain.nodes.VoteAcceptor;
import uk.dsxt.voting.common.utils.InternalLogicException;
import uk.dsxt.voting.common.utils.web.HttpHelper;
import uk.dsxt.voting.common.utils.web.RequestType;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log4j2
public class CryptoVoteAcceptorWeb extends NetworkConnectorDemo implements VoteAcceptor {

    private final static String ACCEPT_VOTE_URL_PART = "/acceptVote";

    private final HttpHelper httpHelper;

    private final String acceptVoteUrl;
    
    private final ObjectMapper mapper = new ObjectMapper();
    
    private final Queue<Map<String, String>> unsentVoteMessages = new ArrayQueue<>();
    
    private final File receiptsFile;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public CryptoVoteAcceptorWeb(String baseUrl, int connectionTimeout, int readTimeout, String receiptsFilePath) {
        super();
        acceptVoteUrl = String.format("%s%s", baseUrl, ACCEPT_VOTE_URL_PART);
        httpHelper = new HttpHelper(connectionTimeout, readTimeout);
        receiptsFile = receiptsFilePath == null || receiptsFilePath.isEmpty() ? null : new File(receiptsFilePath);
        scheduler.scheduleWithFixedDelay(this::sendNextVote, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public NodeVoteReceipt acceptVote(String transactionId, String votingId, BigDecimal packetSize, String clientId, BigDecimal clientPacketResidual, String encryptedData, String voteDigest, String clientSignature) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("transactionId", transactionId);
        parameters.put("votingId", votingId);
        parameters.put("packetSize", packetSize.toPlainString());
        parameters.put("clientId", clientId);
        parameters.put("clientPacketResidual", clientPacketResidual.toPlainString());
        parameters.put("encryptedData", encryptedData);
        parameters.put("voteDigest", voteDigest);
        parameters.put("clientSignature", clientSignature);
        synchronized (unsentVoteMessages) {
            unsentVoteMessages.add(parameters);
        }
        return null;
    }
    
    private void sendNextVote() {
        if (!isNetworkOn)
            return;
        Map<String, String> parameters;
        synchronized (unsentVoteMessages) {
            parameters = unsentVoteMessages.peek();
        }
        if (parameters == null)
            return;
        String result;
        try {
            result = httpHelper.request(acceptVoteUrl, parameters, RequestType.POST);
        } catch (IOException e) {
            log.warn("sendNextVote failed. url={} error={}", acceptVoteUrl, e);
            return;
        } catch (InternalLogicException e) {
            log.error("sendNextVote failed. url={}", acceptVoteUrl, e);
            return;
        }
        if (result == null) {
            log.error("sendNextVote. result == null. url={}", acceptVoteUrl);
            return;
        }
        
        synchronized (unsentVoteMessages) {
            unsentVoteMessages.remove();
        }
        if (receiptsFile != null) {
            try {
                Files.write(receiptsFile.toPath(), Arrays.asList(result), Charset.forName("utf-8"), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
            } catch (IOException e) {
                log.warn("sendNextVote. Couldn't save result to file: {}. error={}", receiptsFile.getAbsolutePath(), e.getMessage());
            }
            
        }
        try {
            NodeVoteReceipt receipt = mapper.readValue(result, NodeVoteReceipt.class);
            log.info("sendNextVote. Vote sent, receipt.status={}", receipt.getStatus());
        } catch (IOException e) {
            log.error("sendNextVote. can not read receipt {}. error={}", result, e.getMessage());
        }
    }
}
