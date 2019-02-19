# Changelog
All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com)

## [Unreleased] 
## [3.0.0]
### Added
- An archive contains informations about its source

### Changed
- ArchiveReader was renamed to NexusArchive
- The methods to retrieve data from an archive is now located on IdxFileLink
- Index file will now be lazily processed. This will speed up the loading of an archive and the direct access of specific data


## [2.0.0]
### Changed
- Package naming: nexusvault.pack -> nexusvault.archive
- Moved public classes & interfaces to nexusvault.archive
- Moved implementation to nexusvault.impl
- Moved .index & .archive file structs to nexusvault.struct

## [1.0.0] - 2019-01-28
### Added
- Support to search through the file-tree stored inside of an index-file
- Support to extract data from an archive