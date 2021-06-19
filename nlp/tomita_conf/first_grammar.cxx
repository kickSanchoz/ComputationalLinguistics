#encoding "utf-8"

Main -> S;
build -> Word<gram="sg",kwtype=building>;
build -> Word<gram="sg,nom",kwtype="build_name">;
S -> build interp (Fact.building);

pers -> Word<gram="sg", kwtype=person>;
S -> pers interp (Fact.person::norm="sg,nom");