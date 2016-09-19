package pebl.test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

import pebl.HasID;
import pebl.HasName;
import pebl.ProcessLanguage;
import pebl.feature.Feature;
import pebl.feature.FeatureDimension;
import pebl.xsd.PathAdapter;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Test implements Comparable<Test>, HasName, HasID, FeatureDimension {

    private final Feature feature;

    private final Path process;
    private final List<TestCase> testCases;
    private final List<Path> files;
    private final String description;
    private final List<TestPartner> partners;
    private final Map<String, String> additionalData;

    public Test() {
        this(Paths.get(""), "", Collections.emptyList(), new Feature());
    }

    public Test(Path process,
            String description,
            List<? extends TestCase> testCases,
            Feature feature) {
        this(process, description, testCases, feature, Collections.emptyList(), Collections.emptyList());
    }

    public Test(Path process,
            String description,
            List<? extends TestCase> testCases,
            Feature feature,
            List<TestPartner> partners) {
        this(process, description, testCases, feature, Collections.emptyList(), partners);
    }

    public Test(Path process,
            String description,
            List<? extends TestCase> testCases,
            Feature feature,
            List<Path> files,
            List<TestPartner> partners) {
        this.partners = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(partners)));
        this.process = Objects.requireNonNull(process);
        this.feature = Objects.requireNonNull(feature);
        this.description = Objects.requireNonNull(description);
        this.testCases = Collections.unmodifiableList(uniqueifyTestCaseNames(new ArrayList<>(Objects.requireNonNull(testCases))));
        this.files = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(files)));
        this.additionalData = Collections.emptyMap();
    }

    public Test(Path process, List<TestCase> testCases,
            Feature feature,
            List<Path> files,
            String description,
            List<TestPartner> partners,
            Map<String, String> additionalData) {
        this.process = process;
        this.testCases = testCases;
        this.feature = feature;
        this.files = files;
        this.description = description;
        this.partners = partners;
        this.additionalData = additionalData;
    }

    public Test withNewProcessAndFeature(Path process, Feature feature) {
        return new Test(process, description, testCases, feature, files, partners);
    }

    public Test withNewTestCases(List<TestCase> testCases) {
        return new Test(process, description, testCases, feature, files, partners);
    }

    @XmlElement
    public List<Path> getFiles() {
        return files;
    }

    private List<TestCase> uniqueifyTestCaseNames(List<TestCase> testCases) {
        // make sure that the tests have different numbers
        for (int counter = 1; counter <= testCases.size(); counter++) {
            TestCase testCase = testCases.get(counter - 1);
            testCase.setNumber(counter);
        }

        // only when not already unique
        if (!hasUniqueTestCaseNames(testCases)) {
            return testCases;
        }

        // make test case names unique
        for (TestCase testCase : testCases) {
            testCase.setName(testCase.getName() + "-" + testCase.getNumber());
        }

        return testCases;
    }

    private boolean hasUniqueTestCaseNames(List<TestCase> testCases) {
        return testCases.stream().map(TestCase::getName).distinct().count() == testCases.size();
    }

    /**
     * Returns the process language used by the given process.
     *
     * @return the process language used by the given process.
     */
    @XmlElement
    public ProcessLanguage getProcessLanguage() {
        String lowerCasePath = getProcess().toString().toLowerCase();
        if (lowerCasePath.endsWith(".bpmn")) {
            return ProcessLanguage.BPMN;
        } else if (lowerCasePath.endsWith(".bpel")) {
            return ProcessLanguage.BPEL;
        } else {
            return ProcessLanguage.UNKNOWN;
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!getClass().equals(o.getClass())) {
            return false;
        }

        Test that = (Test) o;

        return process.toString().equals(that.process.toString());

    }

    public int hashCode() {
        return process.hashCode();
    }

    @Override
    public int compareTo(Test o) {
        return process.compareTo(o.process);
    }

    @XmlElement(required = true)
    public Path getProcess() {
        return this.process;
    }

    @XmlElement(required = true)
    public String getDescription() {
        return description;
    }

    @XmlElement
    public List<TestCase> getTestCases() {
        return testCases;
    }

    private void setTestCases(List<TestCase> testCases) {
        uniqueifyTestCaseNames(testCases);
        this.testCases.clear();
        this.testCases.addAll(testCases);
    }

    @Override
    @XmlElement(required = true)
    public String getName() {
        return getFeature().getName();
    }

    @XmlIDREF
    @XmlElement(required = true)
    public Feature getFeature() {
        return feature;
    }

    public List<Path> getFiles(Predicate<Path> predicate) {
        return getFiles().stream().filter(predicate).collect(Collectors.toList());
    }

    @XmlElement(required = true)
    public List<TestPartner> getTestPartners() {
        return partners;
    }

    public List<TestPartner> getPartners() {
        return partners;
    }

    @XmlElement(required = true)
    public Map<String, String> getAdditionalData() {
        return additionalData;
    }

    @Override
    @XmlID
    @XmlElement(required = true)
    public String getID() {
        return String.join(HasID.SEPARATOR,getFeature().getID(), getName());
    }
}

