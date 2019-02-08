# Changelog
All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com)

## [Unreleased]
### Added
- Support to 'debug' a m3 model, this means to write as many values/structs to a table format, which can, for example, be dumped as csv
- Connection between Creature2DisplayInfo.tbl and m3 (See StructM3Header#model2Display and #display2model)

### Changed
- Naming of StructM3Header#unk_offset_200 and #unk_offset_200 -> #model2Display and #display2model

## [0.6.0] - 2019-01-28
### Added
- Support for different vertex formats
- Support to query for m3 textures
- Basic bone support to build a bone hierarchy and bone location