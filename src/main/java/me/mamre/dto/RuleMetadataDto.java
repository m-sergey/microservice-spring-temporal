package me.mamre.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;
import java.util.Objects;

@JsonTypeName("RuleMetadata")
public class RuleMetadataDto {

    private String key;

    private Integer version;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime uploadedAt;

    public RuleMetadataDto key(String key) {
        this.key = key;
        return this;
    }

    /**
     * Get key
     * @return key
     */

    @Schema(name = "key", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("key")
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public RuleMetadataDto version(Integer version) {
        this.version = version;
        return this;
    }

    /**
     * Get version
     * @return version
     */

    @Schema(name = "version", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("version")
    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public RuleMetadataDto uploadedAt(OffsetDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
        return this;
    }

    /**
     * Get uploadedAt
     * @return uploadedAt
     */
    @Valid
    @Schema(name = "uploadedAt", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("uploadedAt")
    public OffsetDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(OffsetDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RuleMetadataDto ruleMetadata = (RuleMetadataDto) o;
        return Objects.equals(this.key, ruleMetadata.key) &&
                Objects.equals(this.version, ruleMetadata.version) &&
                Objects.equals(this.uploadedAt, ruleMetadata.uploadedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, version, uploadedAt);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RuleMetadataDto {\n");
        sb.append("    key: ").append(toIndentedString(key)).append("\n");
        sb.append("    version: ").append(toIndentedString(version)).append("\n");
        sb.append("    uploadedAt: ").append(toIndentedString(uploadedAt)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
