package com.mit.jna.utils.broadcast;

import com.mit.jna.configuration.YamlPropertySourceFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@PropertySource(value = {"classpath:broadcast.yml"}, factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "broadcast-files")
@Slf4j
public class BroadcastFile {
    private List<AudioFile> files;
}

@Data
class AudioFile {
    private String id;
    private String desc;
    private String path;
}
