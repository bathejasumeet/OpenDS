//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Aenderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.03.08 um 11:36:05 AM CET 
//


package eu.opends.opendrive.geometryGenerator;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the eu.opends.opendrive.geometryGenerator package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _RoadDescription_QNAME = new QName("http://opends.eu/roadDescription", "roadDescription");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: eu.opends.opendrive.geometryGenerator
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RoadDescription }
     * 
     */
    public RoadDescription createRoadDescription() {
        return new RoadDescription();
    }

    /**
     * Create an instance of {@link GeometriesType }
     * 
     */
    public GeometriesType createGeometriesType() {
        return new GeometriesType();
    }

    /**
     * Create an instance of {@link Road }
     * 
     */
    public Road createRoad() {
        return new Road();
    }

    /**
     * Create an instance of {@link LineType }
     * 
     */
    public LineType createLineType() {
        return new LineType();
    }

    /**
     * Create an instance of {@link StartType }
     * 
     */
    public StartType createStartType() {
        return new StartType();
    }

    /**
     * Create an instance of {@link SpiralType }
     * 
     */
    public SpiralType createSpiralType() {
        return new SpiralType();
    }

    /**
     * Create an instance of {@link ArcType }
     * 
     */
    public ArcType createArcType() {
        return new ArcType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RoadDescription }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://opends.eu/roadDescription", name = "roadDescription")
    public JAXBElement<RoadDescription> createRoadDescription(RoadDescription value) {
        return new JAXBElement<RoadDescription>(_RoadDescription_QNAME, RoadDescription.class, null, value);
    }

}
