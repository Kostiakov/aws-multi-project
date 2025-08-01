package lambda;

import java.time.LocalDateTime;
import java.util.Objects;

public class ImageMetadata {

    public ImageMetadata() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getApplicationUrl() {
        return applicationUrl;
    }

    public void setApplicationUrl(String applicationUrl) {
        this.applicationUrl = applicationUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageMetadata that = (ImageMetadata) o;
        return Objects.equals(name, that.name) && Objects.equals(fileExtension, that.fileExtension) &&
                Objects.equals(updateTime, that.updateTime) && Objects.equals(size, that.size) &&
                Objects.equals(applicationUrl, that.applicationUrl);
    }

    @Override
    public String toString() {
        return "ImageMetadata{" +
                "name='" + name + '\'' +
                ", fileExtension='" + fileExtension + '\'' +
                ", updateTime=" + updateTime +
                ", size=" + size +
                ", applicationUrl='" + applicationUrl + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, fileExtension, updateTime, size, applicationUrl);
    }

    private String name;

    private String fileExtension;

    private LocalDateTime updateTime;

    private Long size;

    private String applicationUrl;

}
