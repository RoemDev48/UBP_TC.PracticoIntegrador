
function saludar
end function


function main
s = "hola mundo"
vacia = ""
especial = "linea1\nlinea2\tcolumna"
param s
t0 = call saludar, 1
param "literal directo"
t1 = call saludar, 1
return 0
end function

