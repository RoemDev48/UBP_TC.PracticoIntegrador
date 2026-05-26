
function main
flag = true
flag2 = false
i = 0
L0:
t0 = i < 5
ifFalse t0 goto L1
t1 = i * 10
datos[i] = t1
t2 = i + 1
i = t2
goto L0
L1:
x = 100
t3 = &x
ptr = t3
*ptr = 50
t4 = datos[2]
valor = t4
t5 = x + valor
resultado = t5
return resultado
end function

