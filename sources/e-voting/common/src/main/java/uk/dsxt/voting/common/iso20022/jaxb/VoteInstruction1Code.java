//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.02.25 at 03:58:45 PM GMT+03:00 
//


package uk.dsxt.voting.common.iso20022.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for VoteInstruction1Code.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="VoteInstruction1Code">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="CFOR"/>
 *     &lt;enumeration value="CAGS"/>
 *     &lt;enumeration value="ABST"/>
 *     &lt;enumeration value="WTHH"/>
 *     &lt;enumeration value="NOAC"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "VoteInstruction1Code")
@XmlEnum
public enum VoteInstruction1Code {

    CFOR,
    CAGS,
    ABST,
    WTHH,
    NOAC;

    public String value() {
        return name();
    }

    public static VoteInstruction1Code fromValue(String v) {
        return valueOf(v);
    }

}