package com.calincosma.release.tasks


import org.gradle.api.tasks.TaskAction

class CreateReleaseTag extends BaseReleaseTask {

    CreateReleaseTag() {
        super()
        description = 'Creates a tag for the release.'
    }

    @TaskAction
    void createReleaseTag() {
        def message = extension.tagCommitMessage.get() + " '${tagName()}'."
        if (extension.preCommitText.get()) {
            message = "${extension.preCommitText.get()} ${message}"
        }
        scmAdapter.createReleaseTag(message)
    }
}
