
function calcular
t0 = a + b
resultado = t0
return resultado
end function


function main
x = 5
y = 10.5
param x
param y
t1 = call calcular, 2
res = t1
valor = 'Z'
contador = 0
L0:
t2 = contador < 10
ifFalse t2 goto L1
t3 = contador + 1
contador = t3
t4 = contador == 5
ifFalse t4 goto L3
goto L0
L3:
goto L0
L1:
return 0
end function

