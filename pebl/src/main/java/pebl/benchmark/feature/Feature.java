package pebl.benchmark.feature;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.persistence.oxm.annotations.XmlInverseReference;
import pebl.HasExtensions;
import pebl.HasId;
import pebl.HasName;
import pebl.MapAdapter;

@XmlAccessorType(XmlAccessType.NONE)
public class Feature implements HasId, HasName, HasExtensions {

    @XmlInverseReference(mappedBy="features")
    private final FeatureSet featureSet;

    @XmlElement(required = true)
    private final String name;

    @XmlElement(required = true)
    private final String description;

    @XmlID
    @XmlAttribute(required = true)
    private final String id;

    @XmlElement(name="metric")
    @XmlElementWrapper(name="metrics")
    private final List<Metric> metrics = new LinkedList<>();

    @XmlJavaTypeAdapter(MapAdapter.class)
    private final Map<String, String> extensions = new HashMap<>();

    public Feature addMetric(MetricType metricType) {
        metrics.add(new Metric(metricType, getId()));

        return this;
    }

    public Feature() {
        this(new FeatureSet(), "");
    }

    public Feature(FeatureSet featureSet, String name) {
        this(featureSet, name, "");
    }

    public Feature(FeatureSet featureSet, String name, String description) {
        this.featureSet = Objects.requireNonNull(featureSet);
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);

        this.id = String.join(HasId.SEPARATOR, featureSet.getId(), name);

        this.featureSet.addFeature(this);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Feature feature = (Feature) o;
        return Objects.equals(getId(), feature.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    public String getDescription() {
        return description;
    }

    public FeatureSet getFeatureSet() {
        return featureSet;
    }

    public List<Metric> getMetrics() {
        return metrics;
    }

    @Override
    public Map<String, String> getExtensions() {
        return extensions;
    }

    @Override public Feature addExtension(String key, String value) {
        extensions.put(key, value);

        return this;
    }
}
