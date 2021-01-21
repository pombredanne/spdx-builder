# SPDX-Builder

CI/CD tool to generate Bill-of-Materials reports in SPDX format.

**Status**: Experimental research prototype

(See the [architecture document](docs/architecture.md) for a detailed technical description.)

Converts dependencies for many package managers into a standard
[SPDX](https://spdx.github.io/spdx-spec) tag-value Software Bill-of-Materials file, 
optionally integrating externally detected and curated license details.

Inputs for the SBOM are:

* Package information by YAML files from 
[OSS Review Toolkit](https://github.com/oss-review-toolkit/ort) (ORT) Analyzer.
* Curated license scan results from the REST API of a 
[License Scanning Service](https://github.com/philips-software/license-scanner)
backend service.

## Dependencies

This software requires Java 11 (or later) to run.

## Installation

Build the application using the standard gradle command:
```
./gradlew clean build
```
Then make the resulting files from the `build/install/bin` directory available
in the path.

## Configuration
SPDX-Builder does not require setup configuration.

The ORT Analyzer does not require setup configuration. (the ORT configuration
files address other parts of the ORT suite.)

## Usage
_This application requires Java 11 or higher._

### Basic usage
Primary input of SDPX-Builder is the YAML file that is produced by the
[OSS Review Toolkit](https://github.com/oss-review-toolkit/ort) Analyzer.
This tool interprets the configuration files of a wide range of package managers 
to packages with their metadata in a common ORT file format.

Typical command line invocation of ORT Analyzer is:
```
ort analyze -i <project_directory> -o <result_directory>
```
_Note: To avoid the "this and base files have different roots" error, it is
best practice to always provide an absolute path._

The ORT Analyzer produces an `analyzer-result.yml` file in the indicated
result directory containing the bill-of-materials of all identified packages
in ORT format. (Note that the tool fails if the ORT file already exists.)

This output of the Analyzer can be converted to an SPDX tag-value file using
SPDX-Builder by:
```
spdx-builder -c <config_yaml_file> -o <spdx_file> <ort_yaml_file>
```

_Note: If no output file is specified, the output is written to a file named 
`bom.spdx` in the current directory. (If the file has no extension, `.spdx` 
is automatically appended.)_

### SPDX document information
SPDX documents include common information to indicate the purpose and origin
of the software bill-of-materials. This information is provided in the 
"document" section of the YAML configuration file:
```yaml
document:
  title: "<(Optional) Document title>"
  organization: "<(Optional) Organization name>"
  comment: "<(Optional) Document comment>"
  key: "<(Optional) Document key>"
  namespace: "http://optional/document/namespace/uri"
```

### Including projects in the SPDX file
Since ORT Analyzer lists any potential projects it encounters, SPDX-Builder
only exposes the projects that are explicitly listed in the YAML configuration
file:
```yaml
projects:
  - id: "<Input project identifier>"
    purl: "pkg:type/namespace/name@version"
    excluded:
      - "<scope>" 
```
The "<Input project identifier>" is the identifier that is generated by the ORT
Analyzer during detection. SPDX-Builder lists all projects found in the ORT file,
and marks them with "-" for projects that are skipped and "+" for projects that 
are included in the SPDX output.

In case the project itself represents a package, its Package URL can be provided
in the "purl" field of the project definition.

[GLOB patterns](https://docs.oracle.com/javase/tutorial/essential/io/fileOps.html#glob)
can be used to exclude packages in selected "scopes" per project. The remaining
scopes that are included are logged by SPDX-Builder during processing.

### Manual curation
ORT Analyzer can not always retrieve the (correct) metadata per package, and some 
packages may require an explicit choice which license is used. This information
can be provided as "curations" in the YAML configuration file:
```yaml
curations:
  - purl: "pkg:type/namespace/name@version"
    source: "<vcs_tool>+<transport>://<host_name>[/<path_to_repository>][@<revision_tag_or_branch>][#<sub_path>]"
    license: "<(Optional) License>"
```
The applicable package is identified by the "purl", which references the package
by its Package URL.

Some package managers do not provide the complete or correct location of the 
package source code. For such cases, the "source" location can be provided in SPDX 
format as override. Some examples of valid source URI's are:
- `https://git.myproject.org/MyProject.jar` (Download as source archive)
- `git+https://git.myproject.org/MyProject@r1.0` (Git over HTTP by release tag)
- `git+ssh://git%40github.com/MyProject.git@hashvalue` (Git over SSH by commit hash)
- `git+https://git.myproject.org/MyProject#submodule` (As submodule in a mono repo)

### Integration with license scanner service
To query license information from the [License Scanner Service](https://github.com/philips-software/license-scanner),
add the network location of the License Scanner service by adding the command
line parameter `--scanner <scanner_url>`.

### Skipping directories for analysis by ORT
To speed up ORT Analyzer, a repository configuration file can be added to the
scanned project. By excluding paths via [GLOB patterns](https://docs.oracle.com/javase/tutorial/essential/io/fileOps.html#glob)
in a configuration file, analysis of irrelevant sub-projects can be avoided.

The repository configuration for ORT is provided by an `.ort.yml` file in the 
root of the project:
```yaml
excludes:
  paths:
  - pattern: <glob_pattern>
    reason: <path_reason>
    comment: "Free text"
```

The <path_reason> must be any of:
- BUILD_TOOL_OF
- DATA_FILE_OF
- DOCUMENTATION_OF
- EXAMPLE_OF
- OPTIONAL_COMPONENT_OF
- OTHER
- PROVIDED_BY
- TEST_OF

Other configuration possibilities of the ORT Analyzer can be found in the
[ORT repository configuration file documentation](https://github.com/oss-review-toolkit/ort/blob/master/docs/config-file-ort-yml.md).

_Note: Suppressing "scopes" in the ORT repository configuration has no influence 
on the Analyzer, as it collects all metadata. SPDX-Builder will, however, skip the 
scopes marked for exclusion in the ORT repository configuration file._

## How to test the software
The unit test suite is run via the standard Gradle command:
```
./gradlew clean test
```

## Known issues
(Ticked checkboxes indicate topics currently under development.)

Must-have:
- [x] Support non-SPDX licenses.
- [ ] Limit license identifiers to the SPDX list.
- [ ] Abort if ORT Analyzer raised errors.
- [ ] Pass checksum to scanner and SPDX report.

Should-have:
- [ ] Support output "flavors" for the purpose of the generated SBOM.
- [ ] Include CPE identifiers for referencing CVE/NVD security vulnerabilities.

Others:
- [ ] Support RDF/XML SPDX output format
- [ ] Integration with [Quartermaster (QMSTR)](https://qmstr.org/).

## Contact / Getting help

Submit tickets to the [issue tracker](https://github.com/philips-software/spdx-builder/issues).

## License

See [LICENSE.md](LICENSE.md).

## Credits and references

1. [The SPDX Specification](https://spdx.github.io/spdx-spec) documents the SPDX file
standard.
2. [The ORT Project](https://github.com/oss-review-toolkit) provides a toolset for
generating and analyzing various aspects of the Bill-of-Materials.
