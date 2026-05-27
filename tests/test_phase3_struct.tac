
function main
p1.x = 10
p1.y = 20
t0 = p1.x
t1 = t0 + 1
p1.x = t1
t2 = p1.y
t3 = t2 + 1
p1.y = t3
t4 = v.start
t4.x = 1
t5 = v.start
t5.y = 2
t6 = v.end
t7 = p1.x
t6.x = t7
t8 = v.end
t9 = p1.y
t8.y = t9
t10 = &p1
ptr = t10
t11 = *ptr
t11.x = 100
t12 = *ptr
t13 = t12.x
t14 = *ptr
t15 = t14.y
t16 = t13 + t15
val = t16
return 0
end function

