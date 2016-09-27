package pebl.result;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;

import pebl.benchmark.feature.Metric;

@XmlAccessorType(XmlAccessType.NONE)
public class Measurement {

    @XmlIDREF
    @XmlElement(required = true)
    private final Metric metric;

    @XmlElement(required = true)
    private final Value value;

    Measurement() {
        this(new Metric(), new Value());
    }

    public Measurement(Metric metric, Value value) {
        this.metric = metric;
        this.value = value;
    }

    public Value getValue() {
        return value;
    }

    public Metric getMetric() {
        return metric;
    }
}
