/*
 * Copyright 2024 Web3 Labs Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.web3j.ens;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

public class EnsMetadataResponse {
    public boolean is_normalized;
    public String name;
    public String description;
    public Attribute[] attributes;
    public String url;
    public long last_request_date;
    public int version;
    public String background_image;
    public String image;
    public String image_url;

    public boolean isIs_normalized() {
        return is_normalized;
    }

    public void setIs_normalized(boolean is_normalized) {
        this.is_normalized = is_normalized;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Attribute[] getAttributes() {
        return attributes;
    }

    public void setAttributes(Attribute[] attributes) {
        this.attributes = attributes;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getLast_request_date() {
        return last_request_date;
    }

    public void setLast_request_date(long last_request_date) {
        this.last_request_date = last_request_date;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getBackground_image() {
        return background_image;
    }

    public void setBackground_image(String background_image) {
        this.background_image = background_image;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public static class Attribute {
        public String trait_type;
        public String display_type;
        public Object value;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JacksonException e) {
            return "Error serializing EnsMetadataResponse: " + e.getMessage();
        }
    }
}
