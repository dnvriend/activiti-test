/*
 * Copyright 2015 Dennis Vriend
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.dnvriend

import org.github.dnvriend.activity.ActivitiImplicits._

class UserAndGroupsTest extends TestSpec {
  "IdentityService" should "create a user" in {
    val johnDoeUser = identityService.newUser("John Doe")
    johnDoeUser.setFirstName("John")
    johnDoeUser.setLastName("Doe")
    johnDoeUser.setEmail("john@doe.com")
    identityService.save(johnDoeUser) should be a 'success
    identityService.createUserQuery().userId("John Doe").asList should not be 'empty
    identityService.createUserQuery().userFirstName("John").asList should not be 'empty
    identityService.createUserQuery().userLastName("Doe").asList should not be 'empty
    identityService.createUserQuery().userEmail("john@doe.com").asList should not be 'empty
  }

  it should "create a group" in {
    val engineeringGroup = identityService.newGroup("engineering")
    engineeringGroup.setName("Engineering")
    identityService.save(engineeringGroup)
    identityService.createGroupQuery().groupId("engineering").asList should not be 'empty
    identityService.createGroupQuery().groupName("Engineering").asList should not be 'empty
  }

  it should "create a membership" in {
    identityService.membership("John Doe", "engineering") should be a 'success
    identityService.createGroupQuery().groupMember("John Doe").asList.map(_.getId) should contain("engineering")
  }
}
