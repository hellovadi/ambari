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

var App = require('app');

describe('App.KerberosWizardStep4Controller', function() {
  
  describe('#isSubmitDisabled', function() {
    var controller = App.KerberosWizardStep4Controller.create({});
    var configCategories = Em.A([
      App.ServiceConfigCategory.create({ name: 'Advanced', displayName: 'Advanced'})
    ]);
    var configs = Em.A([
      App.ServiceConfigProperty.create({ name: 'prop1', value: 'someVal1', category: 'Advanced'})
    ]);
    controller.set('stepConfigs', [controller.createServiceConfig(configCategories, configs)]);
    
    it('configuration errors are absent, submit should be not disabled', function() {
      expect(controller.get('stepConfigs')[0].get('errorCount')).to.be.eql(0);
      expect(controller.get('isSubmitDisabled')).to.be.false;
    });

    it('config has invalid value, submit should be disabled', function() {
      var serviceConfig = controller.get('stepConfigs')[0];
      serviceConfig.get('configs').findProperty('name', 'prop1').set('value', '');
      expect(serviceConfig.get('errorCount')).to.be.eql(1);
      expect(controller.get('isSubmitDisabled')).to.be.true;
    });  
  });

  describe('#createServiceConfig', function() {
    var controller = App.KerberosWizardStep4Controller.create({});
    it('should create instance of App.ServiceConfig', function() {
      expect(controller.createServiceConfig([], [])).be.instanceof(App.ServiceConfig);
    });
  });
});

