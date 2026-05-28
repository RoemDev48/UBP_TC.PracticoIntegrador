.data
newline: .asciiz "\n"

.text
.globl main

    # function inicializarPunto

inicializarPunto:
    addiu $sp, $sp, -16
    sw $ra, 4($sp)
    sw $fp, 0($sp)
    move $fp, $sp
    move $s0, $a0
    move $s1, $a1
    move $s2, $a2

    # t0 = *p
    move $t0, $s0
    move $v0, $t0
    move $t0, $v0

    # t0.x = vx
    move $v0, $s1
    sw $v0, 0($t0)

    # t1 = *p
    move $t0, $s0
    move $v0, $t0
    move $t1, $v0

    # t1.y = vy
    move $v0, $s2
    sw $v0, 4($t1)

    # return 0
    li $v0, 0
    move $sp, $fp
    lw $fp, 0($sp)
    lw $ra, 4($sp)
    addiu $sp, $sp, 16
    jr $ra

    # end function
    move $sp, $fp
    lw $fp, 0($sp)
    lw $ra, 4($sp)
    addiu $sp, $sp, 16
    jr $ra

    # function main

main:
    addiu $sp, $sp, -16
    sw $ra, 4($sp)
    sw $fp, 0($sp)
    move $fp, $sp

    # t2 = &pt
    lw $t0, -4($fp)
    addiu $v0, $fp, -4
    move $t2, $v0

    # param t2
    move $a0, $t2

    # param 10
    li $a1, 10

    # param 20
    li $a2, 20

    # t3 = call inicializarPunto, 3
    jal inicializarPunto
    move $t3, $v0

    # t4 = pt.x
    lw $v0, -4($fp)
    move $t4, $v0

    # t5 = t4 + 5
    move $t0, $t4
    li $t1, 5
    add $v0, $t0, $t1
    move $t5, $v0

    # pt.x = t5
    move $v0, $t5
    sw $v0, -4($fp)

    # t6 = pt.y
    lw $v0, -8($fp)
    move $t6, $v0

    # t7 = t6 - 3
    move $t0, $t6
    li $t1, 3
    sub $v0, $t0, $t1
    move $t7, $v0

    # pt.y = t7
    move $v0, $t7
    sw $v0, -8($fp)

    # t8 = pt.x
    lw $v0, -4($fp)
    move $t8, $v0

    # val_x = t8
    move $v0, $t8
    move $s3, $v0

    # t9 = pt.y
    lw $v0, -8($fp)
    move $t9, $v0

    # val_y = t9
    move $v0, $t9
    move $s4, $v0

    # t10 = val_x + val_y
    move $t0, $s3
    move $t1, $s4
    add $v0, $t0, $t1
    move $t0, $v0

    # return t10
    move $v0, $t0
    move $sp, $fp
    lw $fp, 0($sp)
    lw $ra, 4($sp)
    addiu $sp, $sp, 16
    jr $ra

    # end function
    move $sp, $fp
    lw $fp, 0($sp)
    lw $ra, 4($sp)
    addiu $sp, $sp, 16
    jr $ra
