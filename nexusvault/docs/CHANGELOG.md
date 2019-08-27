# Changelog
All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com)

## [3.0.0] - 2019-08-27
### Added
- An archive contains now informations about its source
- NexusArchiveWriter which allows to modify (don't forget to back up!) or create new archive files
- Additional error checks and exceptions in case a file can not be found in the archive

### Changed
- ArchiveReader was renamed to NexusArchive
- The methods to retrieve data from an archive is now located on IdxFileLink
- Index file will now be lazily processed. This will speed up the loading of an archive and the direct access of specific data
- The structure of an index and archive file is now combined in StructArchiveFile
- The structure of AARC and AIDX is now bundled in StructRootPackInfo
- Moved to Java 11
- IdxPath has two additional methods to resolve paths against index files with less exceptions to catch


## [2.0.0] - 2019-02-06
### Changed
- Package naming: nexusvault.pack -> nexusvault.archive
- Moved public classes & interfaces to nexusvault.archive
- Moved implementation to nexusvault.impl
- Moved .index & .archive file structs to nexusvault.struct

## [1.0.0] - 2019-01-28
### Added
- Support to search through the file-tree stored inside of an index-file
- Support to extract data from an archive