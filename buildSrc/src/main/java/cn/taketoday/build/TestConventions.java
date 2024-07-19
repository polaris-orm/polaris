/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.taketoday.build;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.testing.Test;
import org.gradle.testretry.TestRetryPlugin;
import org.gradle.testretry.TestRetryTaskExtension;

import java.util.Map;

class TestConventions {

  void apply(Project project) {
    project.getPlugins().withType(JavaBasePlugin.class, (java) -> configureTestConventions(project));
  }

  private void configureTestConventions(Project project) {
    project.getTasks().withType(Test.class,
            test -> {
              configureTests(project, test);
              configureTestRetryPlugin(project, test);
            });
  }

  private void configureTests(Project project, Test test) {
    test.useJUnitPlatform();
    test.include("**/*Tests.class", "**/*Test.class");
    test.setSystemProperties(Map.of(
            "java.awt.headless", "true",
            "io.netty.leakDetection.level", "paranoid",
            "io.netty5.leakDetectionLevel", "paranoid",
            "io.netty5.leakDetection.targetRecords", "32",
            "io.netty5.buffer.lifecycleTracingEnabled", "true"
    ));
    if (project.hasProperty("testGroups")) {
      test.systemProperty("testGroups", project.getProperties().get("testGroups"));
    }
    test.jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED",
            "--add-opens=java.base/java.util=ALL-UNNAMED",
            "--add-opens=java.base/java.net=ALL-UNNAMED",
            "-Djava.locale.providers=COMPAT");

  }

  private void configureTestRetryPlugin(Project project, Test test) {
    project.getPlugins().withType(TestRetryPlugin.class, testRetryPlugin -> {
      TestRetryTaskExtension testRetry = test.getExtensions().getByType(TestRetryTaskExtension.class);
      testRetry.getFailOnPassedAfterRetry().set(false);
      testRetry.getMaxRetries().set(isCi() ? 3 : 0);
    });
  }

  private boolean isCi() {
    return Boolean.parseBoolean(System.getProperty("CI"));
  }

}
