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

/**
 * Custom view for Notification section for MISC tab
 * Used only in the Install Wizard
 * Show configs for creating Init Notification (@see 'data/HDP2/site_properties' for list of the configs used here)
 *
 * @type {App.NotificationsConfigsView}
 */
App.NotificationsConfigsView = App.ServiceConfigsByCategoryView.extend({

  templateName: require('templates/common/configs/notifications_configs'),

  /**
   * @type {string} "yes|no"
   */
  createNotification: 'no',

  /**
   * @type {string} "tls|ssl"
   */
  tlsOrSsl: 'tls',

  /**
   * Determines if notification configs should be disabled
   * @type {boolean}
   */
  configsAreDisabled: true,

  /**
   * Config with flag for user auth in the notification
   * @type {App.ServiceConfigProperty}
   */
  useAuthConfig: function () {
    return this.get('categoryConfigs').findProperty('name', 'smtp_use_auth');
  }.property(),

  willInsertElement: function () {
    this._super();
    this.set('createNotification', this.get('categoryConfigsAll').findProperty('name', 'create_notification').get('value'));
    this.set('tlsOrSsl', this.get('categoryConfigsAll').findProperty('name', 'mail.smtp.starttls.enable').get('value') ? 'tls' : 'ssl');
    var smtp_use_auth = this.get('categoryConfigsAll').findProperty('name', 'smtp_use_auth');
    var v = (smtp_use_auth.get('value') == 'true');
    smtp_use_auth.set('value', v);
    this.updateCategoryConfigs();
  },

  /**
   * If <code>tlsOrSsl</code> was changed two configs should be updated
   * TLS and SSL can't be enabled in the same time
   * @method onTlsOrSslChanged
   */
  onTlsOrSslChanged: function () {
    var tlsOrSsl = this.get('tlsOrSsl');
    this.get('categoryConfigsAll').findProperty('name', 'mail.smtp.starttls.enable').set('value', tlsOrSsl == 'tls');
    this.get('categoryConfigsAll').findProperty('name', 'mail.smtp.startssl.enable').set('value', tlsOrSsl == 'ssl');
  }.observes('tlsOrSsl'),

  /**
   * Update username and password fields state according to <code>useAuthConfig.value</code>
   * @method onUseAuthConfigChange
   */
  onUseAuthConfigChange: function () {
    var configsToUpdate = ['ambari.dispatch.credential.username', 'ambari.dispatch.credential.password'],
      useAuthConfigValue = this.get('useAuthConfig.value'),
      useAuthConfigIsEditable = this.get('useAuthConfig.isEditable'),
      self = this;
    this.get('categoryConfigs').forEach(function (config) {
      if (configsToUpdate.contains(config.get('name'))) {
        var flag = useAuthConfigIsEditable ? useAuthConfigValue : false;
        self.updateConfig(config, flag);
      }
    });
  }.observes('useAuthConfig.value'),

  /**
   * Update all notification fields state according to <code>createNotification</code>
   * If user select to create notification, all fields become editable and required (also run validation for each)
   * If user select don't create notification, all fields become disabled, not required and valid
   * @method updateCategoryConfigs
   */
  updateCategoryConfigs: function () {
    var createNotification = this.get('createNotification'),
      self = this;
    this.get('categoryConfigs').forEach(function (config) {
      var flag = (createNotification == 'yes');
      self.updateConfig(config, flag);
    });
    this.set('configsAreDisabled', this.get('createNotification') == 'no');
    this.onUseAuthConfigChange();
    this.get('categoryConfigsAll').findProperty('name', 'create_notification').set('value', createNotification);
  }.observes('createNotification'),

  /**
   * According to <code>flag</code>, enable/disable <code>config</code>
   * If <code>flag</code> is true - validate <code>config</code> value
   * If <code>flag</code> is false - mark <code>config</code> as valid
   * @param {App.ServiceConfigProperty} config
   * @param {boolean} flag
   * @method updateConfig
   */
  updateConfig: function (config, flag) {
    config.set('isRequired', flag);
    config.set('isEditable', flag);
    if (flag) {
      config.validate();
    }
    else {
      config.set('errorMessage', '');
      config.propertyDidChange('isValid');
    }
  }

});
