package com.startupidea.yipshare.item;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ItemPayload implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final String category;
    private final String description;
    private final String physicalCondition;
    private final String imageUri;
    private final List<String> loanDurations;

    public ItemPayload(
            String name,
            String category,
            String description,
            String physicalCondition,
            String imageUri,
            List<String> loanDurations) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.physicalCondition = physicalCondition;
        this.imageUri = imageUri;
        this.loanDurations = Collections.unmodifiableList(new ArrayList<>(loanDurations));
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getPhysicalCondition() {
        return physicalCondition;
    }

    public String getImageUri() {
        return imageUri;
    }

    public List<String> getLoanDurations() {
        return loanDurations;
    }
}
