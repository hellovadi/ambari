/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ambari.server.controller.metrics;

import org.apache.ambari.server.configuration.ComponentSSLConfiguration;
import org.apache.ambari.server.controller.internal.AbstractPropertyProvider;
import org.apache.ambari.server.controller.internal.PropertyInfo;
import org.apache.ambari.server.controller.metrics.ganglia.GangliaComponentPropertyProvider;
import org.apache.ambari.server.controller.metrics.ganglia.GangliaHostComponentPropertyProvider;
import org.apache.ambari.server.controller.metrics.ganglia.GangliaHostPropertyProvider;
import org.apache.ambari.server.controller.metrics.ganglia.GangliaPropertyProvider;
import org.apache.ambari.server.controller.metrics.ganglia.GangliaReportPropertyProvider;
import org.apache.ambari.server.controller.metrics.timeline.AMSComponentPropertyProvider;
import org.apache.ambari.server.controller.metrics.timeline.AMSHostComponentPropertyProvider;
import org.apache.ambari.server.controller.metrics.timeline.AMSHostPropertyProvider;
import org.apache.ambari.server.controller.metrics.timeline.AMSPropertyProvider;
import org.apache.ambari.server.controller.metrics.timeline.AMSReportPropertyProvider;
import org.apache.ambari.server.controller.spi.Predicate;
import org.apache.ambari.server.controller.spi.Request;
import org.apache.ambari.server.controller.spi.Resource;
import org.apache.ambari.server.controller.spi.SystemException;
import org.apache.ambari.server.controller.utilities.StreamProvider;
import java.util.Map;
import java.util.Set;

import static org.apache.ambari.server.controller.metrics.MetricsServiceProvider.MetricsService;
import static org.apache.ambari.server.controller.metrics.MetricsServiceProvider.MetricsService.GANGLIA;
import static org.apache.ambari.server.controller.metrics.MetricsServiceProvider.MetricsService.TIMELINE_METRICS;
import static org.apache.ambari.server.controller.spi.Resource.InternalType;

public class MetricsPropertyProviderProxy extends AbstractPropertyProvider {
  private final MetricsServiceProvider metricsServiceProvider;
  private AMSPropertyProvider amsPropertyProvider;
  private GangliaPropertyProvider gangliaPropertyProvider;

  public MetricsPropertyProviderProxy(
    InternalType type,
    Map<String, Map<String, PropertyInfo>> componentPropertyInfoMap,
    StreamProvider streamProvider,
    ComponentSSLConfiguration configuration,
    MetricHostProvider hostProvider,
    MetricsServiceProvider serviceProvider,
    String clusterNamePropertyId,
    String hostNamePropertyId,
    String componentNamePropertyId) {

    super(componentPropertyInfoMap);
    this.metricsServiceProvider = serviceProvider;

    switch (type) {
      case Host:
        createHostPropertyProviders(componentPropertyInfoMap, streamProvider,
          configuration, hostProvider, clusterNamePropertyId, hostNamePropertyId);
        break;

      case HostComponent:
        createHostComponentPropertyProviders(componentPropertyInfoMap, streamProvider,
          configuration, hostProvider, clusterNamePropertyId,
          hostNamePropertyId, componentNamePropertyId);
        break;

      case Component:
        createComponentPropertyProviders(componentPropertyInfoMap, streamProvider,
          configuration, hostProvider, clusterNamePropertyId, componentNamePropertyId);
        break;

      default:
        break;
    }
  }

  private void createHostPropertyProviders(Map<String, Map<String, PropertyInfo>> componentPropertyInfoMap,
                                           StreamProvider streamProvider,
                                           ComponentSSLConfiguration configuration,
                                           MetricHostProvider hostProvider,
                                           String clusterNamePropertyId,
                                           String hostNamePropertyId) {

    this.amsPropertyProvider = new AMSHostPropertyProvider(componentPropertyInfoMap,
                                            streamProvider,
                                            configuration,
                                            hostProvider,
                                            clusterNamePropertyId,
                                            hostNamePropertyId);

    this.gangliaPropertyProvider = new GangliaHostPropertyProvider(componentPropertyInfoMap,
                                            streamProvider,
                                            configuration,
                                            hostProvider,
                                            clusterNamePropertyId,
                                            hostNamePropertyId);
}

  private void createHostComponentPropertyProviders(Map<String, Map<String, PropertyInfo>> componentPropertyInfoMap,
                                            StreamProvider streamProvider,
                                            ComponentSSLConfiguration configuration,
                                            MetricHostProvider hostProvider,
                                            String clusterNamePropertyId,
                                            String hostNamePropertyId,
                                            String componentNamePropertyId) {

    this.amsPropertyProvider = new AMSHostComponentPropertyProvider(
                                            componentPropertyInfoMap,
                                            streamProvider,
                                            configuration,
                                            hostProvider,
                                            clusterNamePropertyId,
                                            hostNamePropertyId,
                                            componentNamePropertyId);

    this.gangliaPropertyProvider = new GangliaHostComponentPropertyProvider(
                                            componentPropertyInfoMap,
                                            streamProvider,
                                            configuration,
                                            hostProvider,
                                            clusterNamePropertyId,
                                            hostNamePropertyId,
                                            componentNamePropertyId);
  }

  private void createComponentPropertyProviders(Map<String, Map<String, PropertyInfo>> componentPropertyInfoMap,
                                                StreamProvider streamProvider,
                                                ComponentSSLConfiguration configuration,
                                                MetricHostProvider hostProvider,
                                                String clusterNamePropertyId,
                                                String componentNamePropertyId) {

    this.amsPropertyProvider = new AMSComponentPropertyProvider(
                                            componentPropertyInfoMap,
                                            streamProvider,
                                            configuration,
                                            hostProvider,
                                            clusterNamePropertyId,
                                            componentNamePropertyId);

    this.gangliaPropertyProvider = new GangliaComponentPropertyProvider(
                                            componentPropertyInfoMap,
                                            streamProvider,
                                            configuration,
                                            hostProvider,
                                            clusterNamePropertyId,
                                            componentNamePropertyId);
  }

  @Override
  public Set<Resource> populateResources(Set<Resource> resources, Request request,
                                         Predicate predicate) throws SystemException {

    MetricsService metricsService = metricsServiceProvider.getMetricsServiceType();

    if (metricsService != null) {
      if (metricsService.equals(GANGLIA)) {
        return gangliaPropertyProvider.populateResources(resources, request, predicate);
      } else if (metricsService.equals(TIMELINE_METRICS)) {
        return amsPropertyProvider.populateResources(resources, request, predicate);
      }
    }

    return resources;
  }
}
