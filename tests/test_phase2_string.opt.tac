
function saludar
end function


function main
s = "hola mundo"
vacia = ""
especial = "linea1\nlinea2\tcolumna"
param s
call saludar, 1
param "literal directo"
call saludar, 1
return 0
end function

