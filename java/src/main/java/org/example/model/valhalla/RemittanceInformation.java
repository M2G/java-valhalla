package org.example.model.valhalla;

import java.util.List;

public value class RemittanceInformation {
    private List<String> unstructured;

    public RemittanceInformation(List<String> unstructured) {
        this.unstructured = unstructured;
    }

    public List<String> getUnstructured() {
        return unstructured;
    }

}
