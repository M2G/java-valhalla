package org.example.model.legacy;

import java.util.List;

public class RemittanceInformationLegacy {
    private List<String> unstructured;

    public RemittanceInformationLegacy() {}

    public List<String> getUnstructured() {
        return unstructured;
    }

    public void setUnstructured(List<String> unstructured) {
        this.unstructured = unstructured;
    }
}
