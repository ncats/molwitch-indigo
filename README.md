# molwitch-indigo
molwitch implementation using Indigo 1.4


##  API Compliance
Molwitch - Indigo is still a work in progress, and some features are yet to be
implemented.  Below is the API Contract Checker result:


| Feature | Compliance Level | Comments|
 | ------ | ---------- | ---------- |
| Extended Tetrahedral| FULLY |  |
| Fingerprint| FULLY |  |
| fullInchi | FULLY ( 999 ) |  |
| fullInchi | NOT_COMPLIANT ( 1 ) |  |
| Valence Error | PARTIALLY ( 2 ) |  |
| Valence Error | NOT_COMPLIANT ( 1 ) |  Pentavalent Carbon Incorrect Valence |
| parse mol wierd parity| FULLY |  |
| inchiKey | FULLY ( 999 ) |  |
| inchiKey | NOT_COMPLIANT ( 1 ) |  |
| Remove Non Descript Hydrogens| NOT_COMPLIANT |  |
| Inchi| FULLY |  |
| Default Fingerprinter| FULLY |  |
| R Group| NOT_COMPLIANT |  |
| Mol Parser| FULLY |  |
| Create Chemical| PARTIALLY |  |
| MolSearcher| NOT_COMPLIANT |  |
| Write Mol | PARTIALLY ( 1 ) |  |
| Write Mol | NOT_COMPLIANT ( 1 ) |  can't write v3000 |
| Problematic Smiles| FULLY |  |
| Chemical Source| FULLY |  |
| Clone Chemical| FULLY |  |
| Atom Alias| PARTIALLY |  |
| mol parser unknown format| FULLY |  |
| Atom Map| NOT_COMPLIANT | Can't Set Atom Map; Can't Clear Atom Map |
| Tetrahedral| FULLY |  |
| Atom Path Traversal| FULLY |  |
| Atom Coords| FULLY |  |
| Cis/Trans| PARTIALLY |  |