# Changelog
All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com)

## [0.7.0] - 2019-08-27
### Added
- Support to 'debug' a m3 model, this means to write as many values/structs to a table format, which can, for example, be dumped as csv
- Connection between Creature2DisplayInfo.tbl and m3 (See StructM3Header#model2Display and #display2model)
- Debug: Vertex to CSV
- Equals & Hash for ModelVertex
- Groups for meshes. Certain meshes are bundled together to a group. See ModelMesh#getMeshGroup
- Some meshes are assigned to specific body parts. See ModelMesh#getMeshToBodyPart
- Access to still unknown vertex fields for ModelGeometry and ModelVertex

### Fixed
- ModelMesh#getVertex(index) did not work for index > 0
- A bug in ModelMesh#getVertex(index) where the function did not return the correct vertex for a given index

### Changed
- Naming of StructM3Header#unk_offset_200 and #unk_offset_200 -> #model2Display and #display2model

## [0.6.0] - 2019-01-28
### Added
- Support for different vertex formats
- Support to query for m3 textures
- Basic bone support to build a bone hierarchy and bone location