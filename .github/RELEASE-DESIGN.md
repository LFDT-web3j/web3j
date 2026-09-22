# Proposed release publication design

This note describes follow-up work; it does not change publication behavior or
authorize a release. See [RELEASE.md](RELEASE.md) for the current workflow and
recovery procedure. Maven Central publication still precedes the downstream
GitHub-release and Javadoc jobs in the current workflow.

## Verified capabilities and limitations

Web3j pins JReleaser **1.22.0**. Its [stage enum][stages] supports `UPLOAD`,
`PUBLISH` and `FULL`; the [validator][validator] defaults to `FULL`.
`UPLOAD` uploads and validates without publishing. `PUBLISH` uses an existing
deployment ID without rebuilding or uploading a bundle, and requires that
deployment to be `VALIDATED`. Repeating it for an already `PUBLISHED` deployment
fails that precondition; retries require remote-state reconciliation.

The [bundle implementation][bundle] combines all configured staging repositories
into one ZIP. One root deployer can therefore replace the 13 independent module
deployments. It must reject duplicate coordinates or conflicting relative paths
and run only the aggregate deploy task, not every subproject deploy task.
This reduces partial publication between modules; it does not create a transaction
across Central, GitHub and the documentation repository.

The [Gradle plugin][gradle-output] writes each project's JReleaser output under
`build/jreleaser`. For the deployer named `central`, [output properties][output]
include `deployMavenCentralCentralNamespace` and
`deployMavenCentralCentralDeploymentId`. Preserve these with the source commit,
version, expected modules, manifests and signed bundle.

There is a failure window: the [upload implementation][upload] logs its deployment
ID before waiting for validation, but returns the ID only after that wait.
A failed wait or terminated runner can leave no ID in `output.properties`.
Persist the upload response immediately in a future implementation; retain logs
and use Portal history when recovery evidence is incomplete.

## Proposed sequence

1. Validate the exact release commit and build without consuming task-output
   caches. Stage all expected modules, verify their bytes and completeness,
   generate Javadocs, and prepare the documentation commit without pushing.
2. Produce one signed aggregate bundle from that verified material. Retain its
   checksum and provenance; reject changes to the recorded payload.
3. Use `JRELEASER_MAVENCENTRAL_STAGE=UPLOAD`; preserve the deployment ID and
   require successful validation. Inspect or test the staged artifacts.
4. Establish and verify the exact tag at the source commit under an approved tag
   protection policy; prepare the draft GitHub release and its assets.
   **A draft release is not a tag reservation or lock.** The [release API][release-api]
   ignores `target_commitish` when a tag already exists. Recheck the tag before
   publication and prevent unauthorized tag mutation.
5. After release approval, use `PUBLISH` with the recorded deployment ID
   (`JRELEASER_MAVENCENTRAL_CENTRAL_DEPLOYMENT_ID`). Reconcile timeouts against
   the [Portal deployment state][portal] before any retry.
6. Confirm Central publication, then publish the prepared GitHub draft and push
   the prepared documentation commit. Keep enough evidence to resume these
   final steps without building or deploying the same version again.

Central publication is the irreversible gate after preparation and verification.
The later GitHub and documentation operations can still fail; they need resumable
completion rather than another full deployment.

## Recovery requirements

- If Central completed and a later job failed, use **Re-run failed jobs** only
  when it excludes the successful deployment job. Never blindly rerun all jobs.
- If the deployment job failed or timed out, rerunning failed jobs can redeploy.
  Inspect every module/deployment first; recover only verified missing work
  through a reviewed procedure. Do not overwrite published coordinates.
- If the tag changed, stop and reconcile the expected commit, actual tag and
  published artifacts. Do not automatically force-move the tag.
- A leftover draft proves neither tag existence nor Central success. Reconcile
  its target and assets before reusing or removing it. Preserve all evidence.

## Proposed Javadoc publication repair

The pinned git-publish **5.0.0** uses native Git for [reset][git-reset],
[commit][git-commit] and [push][git-push]. Its publication API has no
username/password fields; the [5.1.0 notes][git-51] confirm that older Grgit
credential variables stopped working in v5. Neither `GRGIT_USER`/`GRGIT_PASS`
nor 5.1.0-only extension properties repair the pinned version.

Keep 5.0.0 initially and use the canonical
`https://github.com/LFDT-web3j/web3j-docs.git`. In the guarded publication step,
provide a temporary [`GIT_ASKPASS` helper][credentials] that accepts only expected
GitHub prompts and reads the existing step-scoped token from the environment.
Disable inherited credential helpers, set `GIT_TERMINAL_PROMPT=0`, remove the
helper on exit, and keep the token out of URLs, arguments and persistent config.
Set the intended bot's author/committer identity explicitly. Verify the credential
can write to that repository and satisfies its policies before a live release.

A local bare-repository smoke check passed the actual `:core:gitPublishCommit`
path: 392 existing generated files matched byte-for-byte, the fixture README
survived and its remote branch did not move. This supports a non-pushing rehearsal
through reset/copy/commit. It does not test GitHub authentication, push acceptance
or explain the historical live failures, whose logs have expired.

## Proposed release environment

Event guards prevent accidental publication; they do not restrict writers who
can modify workflows. Create a `release` environment with required reviewers
and a `release/**` deployment-branch rule. Move publishing credentials into it
and remove their repository-level copies after migration.

The following is illustrative job configuration, **not an active workflow edit**:

```yaml
jobs:
  release:
    environment: release
  javadocs-release:
    environment: release
  git-release:
    environment: release
```

These names currently also run rehearsal work. Split their publishing portions
into live-only jobs before applying a static environment requirement, so PR and
manual rehearsals continue without production approval or credentials.
Keep validation separate, step-scoped secrets, and the exact live publication
guard. Tag protection is an additional policy decision; an environment alone
does not prevent unrelated writers from moving tags.

[stages]: https://github.com/jreleaser/jreleaser/blob/d791e014c566b45c66428060ed9f78ad121aed13/api/jreleaser-model-api/src/main/java/org/jreleaser/model/api/deploy/maven/MavenCentralMavenDeployer.java#L46-L58
[validator]: https://github.com/jreleaser/jreleaser/blob/d791e014c566b45c66428060ed9f78ad121aed13/core/jreleaser-model-impl/src/main/java/org/jreleaser/model/internal/validation/deploy/maven/MavenCentralMavenDeployerValidator.java#L74-L107
[bundle]: https://github.com/jreleaser/jreleaser/blob/d791e014c566b45c66428060ed9f78ad121aed13/sdks/jreleaser-mavencentral-java-sdk/src/main/java/org/jreleaser/sdk/mavencentral/MavenCentralMavenDeployer.java#L75-L206
[gradle-output]: https://github.com/jreleaser/jreleaser/blob/d791e014c566b45c66428060ed9f78ad121aed13/plugins/jreleaser-gradle-plugin/src/main/groovy/org/jreleaser/gradle/plugin/internal/JReleaserProjectConfigurer.groovy#L68-L75
[output]: https://github.com/jreleaser/jreleaser/blob/d791e014c566b45c66428060ed9f78ad121aed13/core/jreleaser-model-impl/src/main/java/org/jreleaser/model/internal/JReleaserContext.java#L1166-L1177
[upload]: https://github.com/jreleaser/jreleaser/blob/d791e014c566b45c66428060ed9f78ad121aed13/sdks/jreleaser-mavencentral-java-sdk/src/main/java/org/jreleaser/sdk/mavencentral/MavenCentral.java#L145-L153
[release-api]: https://docs.github.com/en/rest/releases/releases#create-a-release
[portal]: https://central.sonatype.org/publish/publish-portal-api/
[git-reset]: https://github.com/ajoberstar/gradle-git-publish/blob/8b81ec492f7ecbd8a7f371eba70a7c3cc2601057/src/main/java/org/ajoberstar/gradle/git/publish/tasks/GitPublishReset.java
[git-commit]: https://github.com/ajoberstar/gradle-git-publish/blob/8b81ec492f7ecbd8a7f371eba70a7c3cc2601057/src/main/java/org/ajoberstar/gradle/git/publish/tasks/GitPublishCommit.java#L60-L78
[git-push]: https://github.com/ajoberstar/gradle-git-publish/blob/8b81ec492f7ecbd8a7f371eba70a7c3cc2601057/src/main/java/org/ajoberstar/gradle/git/publish/tasks/GitPublishPush.java
[git-51]: https://github.com/ajoberstar/gradle-git-publish/releases/tag/5.1.0
[credentials]: https://git-scm.com/docs/gitcredentials
