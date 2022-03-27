# Readme

JSON s dokumenty je k dispozici na: https://drive.google.com/file/d/1fkMwmxUvlLmK8qf22JQ6cCilLwZn0ggv/view?usp=sharing

Pro správnou funkčnost příkladů je potřeba JSON umístit do: src/main/resources/

Příklad použití s Javou je v třídě **App**, kde se provedou CRUD operace a dva příklady vyhledávání. Před spuštěním je nutné pro data
vytvořit index metacritic, což je možné buď spuštěním mainu třídy **CreateIndex**
nebo v Kibaně pomocí requestu (viz soubor kibana_console/create_index.kibana):

`PUT metacritic`

Složka **kibana_console** obsahuje příklady pro CRUD a vyhledávání v developer konzoli v Kibaně.
