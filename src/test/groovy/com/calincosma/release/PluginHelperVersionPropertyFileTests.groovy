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

import com.calincosma.release.tasks.InitScmAdapter
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

public class PluginHelperVersionPropertyFileTests extends Specification {

    Project project

    PluginHelper helper

    File testDir = new File("build/tmp/test/${getClass().simpleName}")

    def setup() {
        project = ProjectBuilder.builder().withName("ReleasePluginTest").withProjectDir(testDir).build()
        project.version = '1.1'
        project.plugins.apply(BasePlugin.class)
        ReleasePlugin releasePlugin = project.plugins.apply(ReleasePlugin.class)
        project.extensions.release.scmAdapters = [TestAdapter]
        releasePlugin.createScmAdapter()

        helper = new PluginHelper(project: project, extension: project.extensions['release'] as ReleaseExtension)

        def props = project.file("gradle.properties")
        props.withWriter {it << "version=${project.version}"}
    }

    def cleanup() {
        if (testDir.exists()) testDir.deleteDir()
    }

    def 'should find gradle.properties by default'() {
        expect:
        helper.findPropertiesFile().name == 'gradle.properties'
    }

    def 'should find properties from convention'() {
        given:
        def props = project.file("custom.properties")
        props.withWriter {it << '@@@@'}
        (project.extensions.release as ReleaseExtension).with {
            versionPropertyFile.set('custom.properties')
        }
        expect:
        helper.findPropertiesFile().name == 'custom.properties'
    }

    def 'by default should update `version` property from props file'() {
        given:
        helper.updateVersionProperty("2.2")
        expect:
        project.file("gradle.properties").readLines()[0] == 'version=2.2'
    }

    def 'when configured then update `version` and additional properties from props file'() {
        given:
        def props = project.file("custom.properties")
        props.withWriter {
            it << "version=${project.version}\nversion1=${project.version}\nversion2=${project.version}\n"
        }
        (project.extensions.release as ReleaseExtension).with {
            versionPropertyFile.set('custom.properties')
            versionProperties.set(['version1'])
        }
        when:
        helper.updateVersionProperty("2.2")
        def lines = project.file("custom.properties").readLines()
        then:
        lines[0] == 'version=2.2'
        lines[1] == 'version1=2.2'
        lines[2] == 'version2=1.1'
    }

    def 'should update version of project and subprojects'() {
        given:
        def proj1 = ProjectBuilder.builder().withParent(project).withName("proj1").build()
        proj1.version = project.version
        def proj2 = ProjectBuilder.builder().withParent(project).withName("proj2").build()
        proj2.version = project.version
        when:
        helper.updateVersionProperty("2.2")
        then:
        assert project.version == '2.2'
        assert proj1.version == project.version
        assert proj2.version == project.version
    }

    def 'should not fail when version contains spaces'() {
        given:
        project.version = '2.5'
        def props = project.file("gradle.properties")
        props.withWriter {
            it << "version = ${project.version}\n"
            it << "version1 : ${project.version}\n"
            it << "version2   ${project.version}\n"
        }
        (project.extensions.release as ReleaseExtension).with {
            versionProperties.set(['version1', 'version2'])
        }
        when:
        (project.tasks.initScmAdapter as InitScmAdapter).initScmAdapter()
        helper.updateVersionProperty('2.6')
        def lines = project.file("gradle.properties").readLines()
        then:
        noExceptionThrown()
        lines[0] == 'version = 2.6'
        lines[1] == 'version1 : 2.6'
        lines[2] == 'version2   2.6'
    }

    def 'should not escape other stuff'() {
        given:
        project.version = '3.0'
        def props = project.file("gradle.properties")
        props.withWriter {
            it << "version=${project.version}\n"
            it << "something=http://www.gradle.org/test\n"
            it << "  another.prop.version =  1.1\n"
        }
        when:
        (project.tasks.initScmAdapter as InitScmAdapter).initScmAdapter()
        helper.updateVersionProperty('3.1')
        def lines = project.file("gradle.properties").readLines()
        then:
        noExceptionThrown()
        lines[0] == 'version=3.1'
        lines[1] == 'something=http://www.gradle.org/test'
        lines[2] == '  another.prop.version =  1.1'
    }

    def 'should not fail on other property separators'() {
        given:
        project.version = '3.2'
        def props = project.file("gradle.properties")
        props.withWriter {
            it << "version:${project.version}\n"
            it << "version1=${project.version}\n"
            it << "version2 ${project.version}\n"
        }
        (project.extensions.release as ReleaseExtension).with {
            versionProperties.set(['version1', 'version2'])
        }
        when:
        helper.updateVersionProperty('3.3')
        def lines = project.file("gradle.properties").readLines()
        then:
        noExceptionThrown()
        lines[0] == 'version:3.3'
        lines[1] == 'version1=3.3'
        lines[2] == 'version2 3.3'
    }
}
