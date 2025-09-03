package com.example.sbz.config;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StreamUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class DroolsConfig {

  @Bean
  public KieContainer kieContainer() {
    try {
      KieServices ks = KieServices.Factory.get();
      KieFileSystem kfs = ks.newKieFileSystem();

      // Učitaj SVE .drl iz classpath-a (npr. rules/auth/*.drl, rules/detection/*.drl)
      PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
      Resource[] drls = resolver.getResources("classpath*:rules/**/*.drl");

      for (Resource res : drls) {
        try (InputStream in = res.getInputStream()) {
          byte[] bytes = StreamUtils.copyToByteArray(in);
          // pokušaj da zadržiš relativni path "rules/…"
          String path = res.getURL().getPath().replace('\\','/');
          int i = path.indexOf("/rules/");
          String sourcePath = (i >= 0) ? path.substring(i + 1) : "rules/" + res.getFilename();

          kfs.write( ks.getResources()
              .newByteArrayResource(bytes, StandardCharsets.UTF_8.name())
              .setSourcePath(sourcePath) );
        }
      }

      KieBuilder kb = ks.newKieBuilder(kfs).buildAll();
      if (kb.getResults().hasMessages(Message.Level.ERROR)) {
        throw new RuntimeException(kb.getResults().toString());
      }
      return ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Bean
  public KieSession kieSession(KieContainer container) {
    return container.newKieSession();
  }
}
