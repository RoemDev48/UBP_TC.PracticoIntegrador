
function main
x = 10
y = 20
t0 = x < y
t1 = x != 0
t2 = t0 && t1
ifFalse t2 goto L0
t3 = x + 5
x = t3
goto L1
L0:
t4 = y - 5
y = t4
L1:
L2:
t5 = x > 0
ifFalse t5 goto L3
t6 = x - 1
x = t6
t7 = x == 5
ifFalse t7 goto L5
goto L2
L5:
goto L2
L3:
total = 0
i = 0
L6:
t8 = i < 10
ifFalse t8 goto L8
t9 = total + i
total = t9
t10 = total > 30
ifFalse t10 goto L10
goto L8
L10:
L7:
t11 = i + 1
i = t11
goto L6
L8:
return total
end function

