# Release validation and rehearsals

The **Release** workflow checks the version/tag, then validates the selected
commit with the reusable Build workflow before staging its Maven publications.
Staging bypasses the Gradle build cache. The separate `verifyReleaseArtifacts`
Gradle task reads the configured projects and Maven publications, excluding
`integration-tests`: every module must supply its binary, sources and Javadoc
JARs, POM and Gradle module metadata. It verifies existing files without invoking
staging or publication, and writes the required-file inventory for the handoff.
The Maven artifact ID must equal the Gradle project name, the group must be
`org.web3j`, and the version must match the supplied release version. Staging
must use each project's `build/staging-deploy` directory. The excluded root and
`integration-tests` projects must have neither a `maven` publication nor a
`localStaging` repository; making either publishable fails verification.

The module set comes from the evaluated `settings.gradle`, not an independent
allowlist. The current count of 13 is descriptive: removing an included module
also removes it from verification. Review module-set changes as release-scope
changes. The verify step records its actual module and file counts in the run
summary. Standalone verification schedules no producers; when staging is also
requested in one invocation, verification runs after it, including with
`--parallel`. The task opts out of the configuration cache because it reads the
evaluated publication model at execution time.

Before recording SHA-256 checksums, every required file must occur in the staged
file set. Checksums cover staged files and the expected-file list. Commit, version
and event are recorded separately. The workflow uploads these files and verifies
them after downloading the exact artifact ID. Central deployment excludes the
Gradle re-staging dependency and checks the recorded hashes before and after
execution. Signing can add `.asc` files. A post-deployment failure detects a
problem but cannot undo publication.

**The live publication path is not transactional.** Central deployment still
precedes downstream download verification and Javadoc generation. Each of the
13 modules has a separate Central deployment. A later failure can leave partial
publication. Read the recovery instructions before publishing; the larger
[publication redesign and Javadoc authentication fix](RELEASE-DESIGN.md) are
proposals, not implemented behavior.

## Test before merging

Pull requests to `main` that change the configured build/release paths run a
rehearsal automatically, including drafts. A PR rehearsal tests GitHub's merge
commit, recorded in `release-metadata.txt`, rather than only the PR head.

Once this workflow is on the default branch, **Actions → Release → Run workflow**
can select a branch. Manual runs are always rehearsals, even on `release/*`.
Before merging, use the PR-triggered run. **Re-run all jobs** is suitable for a
rehearsal; do not use it to recover a live publication.

Rehearsals use `0.0.0-rehearsal.<run-id>.<attempt>` and provide two downloads:

- `release-artifacts-<version>-<attempt>`: staged module files, checksum manifest,
  required-file list and source record.
- `release-javadocs-<version>`: generated core API documentation.

After the package upload, a rehearsal-only self-test temporarily removes one
module metadata file. Verification must reject the missing file without
regenerating it or retaining an inventory. The self-test restores the file,
verifies again, and compares the inventory and payload checksums with the
uploaded state. It receives no secrets and is skipped for push events.
The intentional Gradle failure appears in its log; the self-test step itself
must finish successfully.

To verify locally, first stage using the same explicit **non-SNAPSHOT** version:

```sh
VERSION=0.0.0-rehearsal.local
./gradlew "-Pversion=$VERSION" publishMavenPublicationToLocalStagingRepository --no-build-cache
./gradlew "-Pversion=$VERSION" :verifyReleaseArtifacts --no-build-cache
```

The second command only checks existing files. Omitting `-Pversion` selects the
repository's default `-SNAPSHOT` version, whose timestamped Maven staging files
do not match this release verifier's expected filenames. It cannot verify that
default snapshot layout. Retained artifacts can be checked without compiling
by restoring their original staging paths and supplying their recorded version.

The workflow requests 14 days of retention, but the repository currently caps
this at 7 days. Check actual expiry and retain recovery evidence before it expires.

Rehearsals do not receive publishing/signing credentials. Central deployment,
GitHub release/tag creation and Javadoc publication are skipped. Registry
credentials can still be used by validation. Rehearsals verify packaging and
artifact transfer, not production signing, authentication or service acceptance.
A separate local smoke check of git-publish 5.0.0 exercised reset/copy/commit
against a bare fixture without pushing; it did not test GitHub authentication.

PRs matching the path filter run Build twice: the normal PR Build and Release's
reusable validation. This deliberately keeps release validation self-contained
and adds another build plus an integration suite (roughly 15–18 minutes observed).
Newer PR/manual rehearsals cancel older runs for the same event/ref. Live push
runs are never cancelled by this policy.

Push-triggered validation, including release validation, disables Gradle's task
output cache so tests execute for that checkout; dependency downloads can still
be cached. PR/manual validation retains task output caching. Staging disables
it for every event, including rehearsals. CI formatting uses the checked-in
license/formatter inputs by excluding their two download tasks. Other existing
shared build-fragment downloads and mutable dependencies remain; these changes
do not make the build reproducible or entirely independent of the network.
The existing Spotless `groovyGradle` target is `*.gradle` in each project
directory; it does not include `gradle/release-validation.gradle`. A passing
Spotless check therefore does not establish formatting coverage for that script.

Do not make path-filtered Release checks globally required: `preflight`,
`validate / build`, `validate / integration-test`, `release`, `javadocs-release`
and `git-release` do not report on PRs outside the filter. Choose requirements
from workflows that run for every applicable PR.

## Publish a release

A push to `release/<version>` is a live publication, not a test. Only that event
enables the three publishing steps after validation. The version must match the
workflow's non-snapshot release format, for example `6.0.0` or `6.0.0-rc.1`.
Hyphenated versions are marked prerelease and are not made the latest GitHub
release. An existing `v<version>` tag must resolve to the validated commit;
annotated tags are dereferenced in the tag namespace.

A release branch cut from an old tag executes the workflow from that old commit,
not the current default branch. Ensure the intended release commit actually
contains these fixes before pushing a release branch.

Publishing remains a maintainer decision. A successful rehearsal neither merges
the PR nor authorizes publication. The live Javadoc step has an unresolved
explicit Git authentication/identity configuration gap; generation success does
not establish push readiness. Review the linked proposal before relying on it.

## Recover an incomplete live publication

Stop automatic retries first. Preserve the original source SHA, version,
artifacts and hashes, logs, tag SHA, each module's Central deployment ID/status,
and any `<module>/build/jreleaser/output.properties`. An upload can exist even
when the job times out before writing its ID; check the logs and Portal history.
Do not assume a failed job made no external changes.

| Situation | Maintainer action |
| --- | --- |
| Central job succeeded, GitHub release or docs job failed | Confirm all expected coordinates are published, then use **Re-run failed jobs** only if it excludes the successful Central job. It should reuse the original artifact ID. Never use **Re-run all jobs**. Resolve authentication/permission failures before retrying. |
| Central job failed, timed out, or only some modules appeared | Do not use either rerun option blindly: failed-job retries would execute Central again. Reconcile all 13 coordinates and deployment states. Retain published bytes; use a separately reviewed recovery for only the verified missing publications, or a new version if consistency cannot be established. Already-published coordinates cannot be overwritten. |
| Tag changed during staging/publication | Record expected and actual tag SHA and what reached Central. Stop GitHub release completion and resolve identity as a maintainer decision. Never force-move the tag automatically or redeploy just to repair GitHub metadata. |
| Leftover draft release | Compare its tag, intended SHA, assets and Central state with the retained attempt. A draft proves neither tag reservation nor publication. Reuse or remove it only after reconciliation; do not start a fresh full deploy. The current workflow does not itself create drafts. |
| GitHub release already exists | The create step fails closed. Inspect and reconcile that release's tag/assets rather than deleting it or rerunning deployment automatically. |

Even **Re-run failed jobs** is unsafe when the failed job itself deploys to
Central. The current pipeline has no automated partial-publication recovery.

## Release controls recommended as a follow-up

Event guards prevent accidental publication, but do not restrict a repository
writer who can change workflows or push release branches. Recommend a `release`
environment with required reviewers and a `release/**` deployment-branch rule,
with publishing secrets moved into it. This requires an administrator decision;
no environment or repository policy is configured by this PR.

The workflow adoption change would be `environment: release` on each publishing
job: `release`, `javadocs-release` and `git-release`. See the exact example and
rehearsal implications in [the design proposal](RELEASE-DESIGN.md).

## Snapshot publication

Snapshot runs are serialized. Only successful same-repository main-push Builds
qualify. The workflow checks their SHA against remote `main` before setup and
again before publishing; stale runs skip cleanly. Only the publish step receives
Central username/password. Main can still advance during compilation/upload;
these checks do not make a branch read and remote publication atomic.
