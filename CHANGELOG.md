# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)

## [1.7.13] - build 226 2024-04-18
### Added
- Option to submit only current record to Collect;

## [1.7.12] - build 225 2024-04-10
### Fixed
- Fixed cannot delete records from records list;

## [1.7.12] - build 224 2024-04-03
### Added
- Record edit lock;
- Improved backup/restore;

## [1.7.12] - build 223 2023-08-23
### Fixed
- Smart next button: navigate only to relevant nodes;

## [1.7.11] - build 222 2023-07-20
### Added
- Taxon attribute: show vernacular name in summary list;

## [1.7.11] - build 221 2023-07-19
### Added
- Taxon attribute: store vernacular name if selected from autocomplete;

## [1.7.10] - build 220 2023-07-13
### Fixed
- Fixed error entering invalid date/time in input fields;

## [1.7.10] - build 219 2023-06-13
### Fixed
- Fixed error exporting data when multiple attribute is marked as 'include in summary list';

## [1.7.10] - build 218 2023-06-03
### Fixed
- Fixed long description being shown inside scrollbar in multiple entity summary view;

## [1.7.10] - build 217 2023-06-02
### Added
- Descriptions: show max 3 lines and use show more/less;

## [1.7.9] - build 216 2023-05-29
### Fixed
- Fixed storage permissions on Android 13+;

## [1.7.9] - build 215 2023-05-27
### Added
- Entities summary list: include attributes marked with "Show in summary";

## [1.7.9] - build 214 2023-05-15
### Fixed
- Settings: fixed error selecting custom working directory (Android 11+);

## [1.7.9] - build 213 2023-04-29
### Added
- Taxon attribute autocomplete: added UNK/UNL items;

## [1.7.9] - build 212 2023-04-19
### Fixed
- Avoid runtime errors;

## [1.7.9] - build 211 2023-04-18
### Fixed
- Layout improvements;

## [1.7.9] - build 210 2023-04-05
### Added
- Improved data export speed;

## [1.7.9] - build 209 2023-04-03
### Fixed
- Data export never completes;

## [1.7.9] - build 208 2023-03-28
### Fixed
- Numeric attribute formatting;

## [1.7.9] - build 207 2023-03-17
### Added
- Settings; option to lock screen in portrait mode; font size;

## [1.7.9] - build 206 2023-01-20
### Fixed
- Expressions: evaluations of calculated attributes inside single entities;
### Added
- Expressions: support idm:lookup function;

## [1.7.9] - build 205 2023-01-05
### Added
- File attribute: support generic type "Document";

## [1.7.9] - build 202 2022-11-14
### Added
- File attribute (image): resize captured image to fix max size (if specified);
- File attribute (image): added button to rotate image;

## [1.7.9] - build 201 2022-08-07
### Fixed
- Fixed sampling point items label not showing in deeper levels;

## [1.7.9] - build 200 2022-06-29
### Fixed
- Fixed numeric and text values being deleted randomly;

## [1.7.9] - build 199 2022-06-24
### Added
- Sampling Point Data: use info column label_XX to give labels to sampling point items;

## [1.7.9] - build 198 2022-02-04
### Added
- Data export: added option to export data without calculated attributes (faster);

## [1.7.9] - build 197 2021-10-02
### Fixed
- Fixed errors evaluating numeric calculated attributes giving null values;

### Added
- Code calculated attributes: show code list item code and label;

## [1.7.9] - build 196 2021-10-01
### Fixed
- Fixed error capturing images for file attributes;

## [1.7.9] - build 195 2021-09-28
### Added
- Improved error message for multiple attribute types not supported;
### Fixed
- Double check inserted value on node update;

## [1.7.9] - build 194 2021-09-08
### Added
- Show loading spinner while saving text/numeric attributes;
### Fixed
- Prevented runtime errors (insert missing nodes on record selection);

## [1.7.9] - build 193 2021-09-03 
### Added
- Show error dialog if unexpected error occurs on record creation;
- Improved Working Directory selection;

## [1.7.8] - build 192 2021-05-03 
### Fixed
- Validation error messages not showing properly in coordinate attributes;

## [1.7.8] - build 191 2021-04-24 
### Fixed
- Validation errors not appearing in multiple attributes/entities.

## [1.7.8] - build 190 2021-04-21 
### Added
- Layout adjustments (scrolling long node definition tooltips/descriptions);
- Updated page indicator;
- Updated file chooser;

## [1.7.8] - build 189 2021-04-01
### Added
- Improved Working Directory chooser;

## [1.7.8] - build 188 2021-03-01
### Added
- Support text attribute auto-uppercase;

## [1.7.8] - build 187 2021-01-29
### Fixed
- Error using geometry (polygon) in text attribute;

## [1.7.8] - build 186 2021-01-09
### Fixed
- Default code item description to survey default language description;

## [1.7.8] - build 185 2021-01-09
### Added
- Show code item description below label;

## [1.7.8] - build 184 2020-12-31
### Fixed
- Fixed error importing surveys in Android 6/7

## [1.7.8] - build 183 2020-12-22
### Fixed
- Fixed error importing survey generated with Collect v3

## [1.7.8] - build 182 2020-12-21
### Added
- Use latest OF Collect core version (4.0.0)

## [1.7.7] - build 181 2020-09-24
### Added
- Data export: option to export only current record

## [1.7.6] - build 180 2020-08-18
### Fixed
- Improved loading of species list items
### Added
- Backup options (internal / to new SD card)

## [1.7.6] - build 179 2020-07-24
### Fixed
- Duplicate items in hierarchical code attributes

## [1.7.6] - build 178 2020-04-23
### Fixed
- Coordinate attribute / navigate to expected location crashes on start when coordinate is blank

## [1.7.6] - build 177 2020-04-14
### Fixed
- Record export on survey with multiple keys

## [1.7.6] - build 176 2020-03-24
### Fixed
- Altitude not recorded properly

## [1.7.6] - build 175 2020-03-05
### Added
- Altitude and accuracy in coordinate attributes

## [1.7.5] - build 174 2020-03-02
### Fixed
- Code label not shown in records list

## [1.7.5] - build 173 2020-03-01
### Fixed
- Error capturing images on Android 4.4 #2

## [1.7.5] - build 172 2020-02-29
### Fixed
- Error capturing images on Android 4.4

## [1.7.5] - build 171 2020-02-10
### Fixed
- Default value applied on relevance change

## [1.7.5] - build 170 2020-02-02
### Fixed
- Data export not showing "Share with" screen

## [1.7.5] - build 169 2020-01-29
### Fixed
- Permissions not being asked in Android 6+

## [1.7.5] - build 168 2020-01-27
### Fixed
- Images not captured in certain devices

## [1.7.5] - build 167 2020-01-22
### Fixed
- Table view: fixed hidden nodes showing in table

## [1.7.5] - build 166 2020-01-17
### Added
- Improved permissions request (added rationale messages)
- Allow single code attribute deselection
- Support surveys generated with Collect 3.25.x
### Fixed
- Hide calculated attributes not marked with "Show in entry form" from Table View

## [1.7.4] - build 165 2019-09-10
### Fixed
- Calculated one-time hidden attributes issues

## [1.7.3] - build 163 2019-05-09
### Fixed
- Allow usage of large Survey Guide files (more than 2MB)

## [1.7.3] - build 162 2019-05-07
### Added
- Support Interview Label

## [1.7.3] - build 161 2019-04-30
### Fixed
- Allow remote Collect server connection in Android 8+

## [1.7.3] - build 160 2019-04-18
### Fixed
- Record creation and modified dates in data export

## [1.7.3] - build 158 2019-04-16
### Fixed
- Language labels in Settings not showing in some devices

## [1.7.3] - build 157 2019-04-16
### Added
- About page

### Fixed
- "One-time" calculated attribute export issue

## [1.7.2] - build 156 2019-04-11
### Added
- Support Survey Guide files (if attached to the survey)

### Fixed
- Error importing survey on certain devices
