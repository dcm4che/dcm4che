````
usage: fixlo2un SOURCE DEST
or fixlo2un SOURCE... DIRECTORY

Utility to fix length of private (gggg,00ee) elements truncated to 2 bytes
on conversion from implicit VR to explicit VR Transfer Syntax
-
Options:
 -h,--help      display this help and exit
 -V,--version   output version information and exit
-
Example: fixlo2un corrupted.dcm fixed.dcm
corrupted.dcm -> fixed.dcm
=> Results in
2008: (0043,0042) LO #66 -> UN #4718658
4720674: (0045,0044) LO #68 -> UN #4390980
9111662: (0047,0047) LO #70 -> UN #4522054
24709496: (004D,004B) LO #76 -> UN #4849740
29559244: (004F,004F) LO #78 -> UN #5111886
34671138: (0053,0052) LO #82 -> UN #5242962
39914108: (0055,0054) LO #84 -> UN #5439572
45353688: (0057,0057) LO #86 -> UN #5570646
50924342: (0059,0059) LO #88 -> UN #5701720
68357220: (005D,005C) LO #92 -> UN #6029404
74386632: (005D,005D) LO #94 -> UN #6094942
80481582: (005F,005F) LO #96 -> UN #6226016
86707606: (0061,005F) LO #96 -> UN #6226016
92933630: (0065,006C) LO #126 -> UN #9371774
-
````
