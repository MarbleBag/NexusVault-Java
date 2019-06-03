# Changelog
All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com)

## [2.0.0 Unreleased]
### Changed
- Additional attributes are available on TextureObject

## [1.0.1] - 2019-02-09
### Fixed 
- Compressed textures of type 1 & 2 with non-equal-sized edges will no longer lose some of their blocks 
- TextureReader#acceptFileSignature will now correctly return 'true' if the signature is accepted

## [1.0.0] - 2019-01-28
### Changed
- Swapped default IDCT with Chen-Wang's algorithm for integer IDCT