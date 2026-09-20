# Release validation and rehearsals

The **Release** workflow validates the selected commit with the reusable Build
workflow before staging its Maven publications. It records SHA-256 checksums and
the commit/version, uploads the staged files, then downloads and verifies them in
a separate job. Javadocs are also generated from the same commit.

## Test before merging

Pull requests to `main` that change the build or release configuration run a
release rehearsal automatically, including draft pull requests. Open the PR's
**Release** check to follow the run. A PR rehearsal tests GitHub's merge commit;
the exact commit is recorded in the run and in `release-metadata.txt`.

Once this workflow is available on the default branch, it can also be started
from **Actions → Release → Run workflow**, selecting the branch to test. Manual
runs are always rehearsals, including when a `release/*` branch is selected.
Before merging, use the PR-triggered run and its **Re-run all jobs** button.

Rehearsals use `0.0.0-rehearsal.<run-id>.<attempt>` and provide two downloads on the
workflow run page. The workflow requests 14 days of retention, but the repository
currently caps this at 7 days. Check the artifact's actual expiry before relying
on its availability:

- `release-artifacts-<version>-<attempt>`: module JARs, sources, Javadocs, POMs, Gradle module
  metadata and checksums, plus the workflow's checksum manifest and source record.
- `release-javadocs-<version>`: the generated core API documentation.

Rehearsals do not receive publishing or signing credentials. Maven Central
deployment, GitHub release/tag creation and Javadoc publication are skipped.
This verifies local packaging and the GitHub Actions artifact handoff; it does
not validate signing credentials or the external publishing services.

## Publish a release

A push to `release/<version>` is a **live publication**, not a test. Only this
event enables the three publishing steps, after validation. The version must be
a non-snapshot semantic version, for example `release/6.0.0` or
`release/6.0.0-rc.1`. An existing `v<version>` tag must identify the validated commit.

Publishing remains a maintainer decision. A successful rehearsal does not merge
the PR or authorize a release.
