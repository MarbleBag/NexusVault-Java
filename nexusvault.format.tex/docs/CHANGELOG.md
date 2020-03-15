# Changelog
All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com)

## [3.1.1] - 2020-03-15
### Fixed
- Images compressed as jpgs weren't converted correctly when their width and height aren't equal.

## [3.1.0] - 2020-03-07
### Added
- BufferedImage to TextureImage converter

## [3.0.0] - 2020-03-02
### Added
- Texture image writer. It's now possible to create texture images with TextureWriter. JPG is currently not supported.
- MipMap generator

### Changed
- Large parts of the library are rewritten to improve code organization and maintainability


## [2.0.0] - 2019-08-27
### Changed
- Additional attributes are available on TextureObject

## [1.0.1] - 2019-02-09
### Fixed 
- Compressed textures of type 1 & 2 with non-equal-sized edges will no longer lose some of their blocks 
- TextureReader#acceptFileSignature will now correctly return 'true' if the signature is accepted

## [1.0.0] - 2019-01-28
### Changed
- Swapped default IDCT with Chen-Wang's algorithm for integer IDCT