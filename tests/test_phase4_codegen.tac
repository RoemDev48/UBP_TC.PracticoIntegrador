
function inicializarPunto
t0 = *p
t0.x = vx
t1 = *p
t1.y = vy
return 0
end function


function main
t2 = &pt
param t2
param 10
param 20
t3 = call inicializarPunto, 3
t4 = pt.x
t5 = t4 + 5
pt.x = t5
t6 = pt.y
t7 = t6 - 3
pt.y = t7
t8 = pt.x
val_x = t8
t9 = pt.y
val_y = t9
t10 = val_x + val_y
return t10
end function

