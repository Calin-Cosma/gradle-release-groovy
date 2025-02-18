/*
 * This file is part of the gradle-release plugin.
 *
 * (c) Eric Berry
 * (c) ResearchGate GmbH
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.calincosma.release

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class ReleaseExtensionTests extends Specification {

    Project project
    ReleaseExtension extension

    def testDir = new File("build/tmp/test/${getClass().simpleName}")

    def setup() {
        project = ProjectBuilder.builder().withName('ReleaseExtensionTest').withProjectDir(testDir).build()
        project.apply plugin: ReleasePlugin

        extension = new ReleaseExtension(project, [:])
        extension.scmAdapters = [TestAdapter]
    }

    def 'test dynamic getting property on extension fails for non adapter'() {
        when:
        extension.footest
        then:
        thrown(MissingPropertyException)
    }

    def 'test dynamic setting property on extension fails for non adapter'() {
        when:
        extension.footest = null
        then:
        thrown(MissingPropertyException)
    }

    def 'test dynamic setting callback on extension fails for non adapter'() {
        when:
        extension.footest {
            testOption = '1234'
        }
        then:
        thrown(MissingMethodException)
    }
}
