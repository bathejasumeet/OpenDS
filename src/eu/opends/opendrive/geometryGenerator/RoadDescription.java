//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Aenderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.03.08 um 11:36:05 AM CET 
//


package eu.opends.opendrive.geometryGenerator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse fuer roadDescription complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="roadDescription">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="road" type="{http://opends.eu/roadDescription}road"/>
 *       &lt;/all>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "roadDescription", propOrder = {

})
@XmlRootElement(name="roadDescription", namespace="http://opends.eu/roadDescription")
public class RoadDescription {

    @XmlElement(required = true)
    protected Road road;

    /**
     * Ruft den Wert der road-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Road }
     *     
     */
    public Road getRoad() {
        return road;
    }

    /**
     * Legt den Wert der road-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Road }
     *     
     */
    public void setRoad(Road value) {
        this.road = value;
    }

}
