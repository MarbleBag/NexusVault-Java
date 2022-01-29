# Nexusvault

Provides code to read and write .index and .archive files and various WS specific file types in Java.

## Supported file formats
### File format: tex
Texture format.

General class: **nexusvault.format.tex.Texture**

- Read via: **nexusvault.format.tex.TextureReader**
- Written via: **nexusvault.format.tex.TextureWriter**

### File format: tbl
A table like format with columns and rows.

General class: **nexusvault.format.tbl.Table**

- Read via: **nexusvault.format.tbl.TableReader**
- Written via: **nexusvault.format.tbl.TableWriter**

### File format: bin
Contains localized text

General class: **nexusvault.format.bin.LanguageDictionary**

- Read via: **nexusvault.format.bin.LanguageReader**
- Written via: **nexusvault.format.bin.LanguageWriter**

### File format: m3
Wildstar Models (Meshes)

General class: **nexusvault.format.m3.Model**

- Read via: **nexusvault.format.m3.ModelReader**
- No write support at this point

Supported features:
* Meshes
* Joints
* Textures

### File format: index & archive
Packed files which contains the assets of WS. 
The index file provides a file system like structure to reference assets via path, while an archive file is basically a lookup table for said assets.

Both can be processed directly via **nexusvault.vault.index.PackedIndexFile**, **nexusvault.vault.archive.PackedArchiveFile**<br>
Or in union via **nexusvault.vault.NexusArchive**, which will manage both: index and archive.

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Versioning

This project is versioned with [SemVer](http://semver.org/)
and stores a list of major changes in its [changelog](CHANGELOG.md), including upcoming changes

## License

This project is licensed under the GNU AGPLv3 License - see the [LICENSE.md](LICENSE.md) file for details

