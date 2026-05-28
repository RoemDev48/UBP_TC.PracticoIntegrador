.data
newline: .asciiz "\n"

.text
.globl main

    # function sumarDoce

sumarDoce:
    addiu $sp, $sp, -8
    sw $ra, 4($sp)
    sw $fp, 0($sp)
    move $fp, $sp
    move $s7, $a0
    move $t0, $a1
    move $t1, $a2
    move $t2, $a3
    lw $v0, 36($fp)
    move $t3, $v0
    lw $v0, 32($fp)
    move $s0, $v0
    lw $v0, 28($fp)
    move $s1, $v0
    lw $v0, 24($fp)
    move $s2, $v0
    lw $v0, 20($fp)
    move $s3, $v0
    lw $v0, 16($fp)
    move $s4, $v0
    lw $v0, 12($fp)
    move $s6, $v0
    lw $v0, 8($fp)
    move $s5, $v0

    # t0 = v1 + v2
    move $t0, $s7
    move $t1, $t0
    add $v0, $t0, $t1
    move $t4, $v0

    # t1 = t0 + v3
    move $t0, $t4
    add $v0, $t0, $t1
    move $t5, $v0

    # t2 = t1 + v4
    move $t0, $t5
    move $t1, $t2
    add $v0, $t0, $t1
    move $t6, $v0

    # t3 = t2 + v5
    move $t0, $t6
    move $t1, $t3
    add $v0, $t0, $t1
    move $t7, $v0

    # t4 = t3 + v6
    move $t0, $t7
    move $t1, $s0
    add $v0, $t0, $t1
    move $t8, $v0

    # t5 = t4 + v7
    move $t0, $t8
    move $t1, $s1
    add $v0, $t0, $t1
    move $t9, $v0

    # t6 = t5 + v8
    move $t0, $t9
    move $t1, $s2
    add $v0, $t0, $t1
    move $t0, $v0

    # t7 = t6 + v9
    move $t1, $s3
    add $v0, $t0, $t1
    move $t1, $v0

    # t8 = t7 + v10
    move $t0, $t1
    move $t1, $s4
    add $v0, $t0, $t1
    move $t4, $v0

    # t9 = t8 + v11
    move $t0, $t4
    move $t1, $s6
    add $v0, $t0, $t1
    move $t2, $v0

    # t10 = t9 + v12
    move $t0, $t2
    move $t1, $s5
    add $v0, $t0, $t1
    move $t5, $v0

    # sum = t10
    move $v0, $t5
    move $s7, $v0

    # return sum
    move $v0, $s7
    move $sp, $fp
    lw $fp, 0($sp)
    lw $ra, 4($sp)
    addiu $sp, $sp, 8
    jr $ra

    # end function
    move $sp, $fp
    lw $fp, 0($sp)
    lw $ra, 4($sp)
    addiu $sp, $sp, 8
    jr $ra

    # function main

main:
    addiu $sp, $sp, -8
    sw $ra, 4($sp)
    sw $fp, 0($sp)
    move $fp, $sp

    # param 1
    li $a0, 1

    # param 2
    li $a1, 2

    # param 3
    li $a2, 3

    # param 4
    li $a3, 4

    # param 5
    li $t0, 5
    addiu $sp, $sp, -4
    sw $t0, 0($sp)

    # param 6
    li $t0, 6
    addiu $sp, $sp, -4
    sw $t0, 0($sp)

    # param 7
    li $t0, 7
    addiu $sp, $sp, -4
    sw $t0, 0($sp)

    # param 8
    li $t0, 8
    addiu $sp, $sp, -4
    sw $t0, 0($sp)

    # param 9
    li $t0, 9
    addiu $sp, $sp, -4
    sw $t0, 0($sp)

    # param 10
    li $t0, 10
    addiu $sp, $sp, -4
    sw $t0, 0($sp)

    # param 11
    li $t0, 11
    addiu $sp, $sp, -4
    sw $t0, 0($sp)

    # param 12
    li $t0, 12
    addiu $sp, $sp, -4
    sw $t0, 0($sp)

    # t11 = call sumarDoce, 12
    jal sumarDoce
    addiu $sp, $sp, 32
    move $t3, $v0

    # result = t11
    move $v0, $t3
    move $s0, $v0

    # return 0
    li $v0, 0
    move $sp, $fp
    lw $fp, 0($sp)
    lw $ra, 4($sp)
    addiu $sp, $sp, 8
    jr $ra

    # end function
    move $sp, $fp
    lw $fp, 0($sp)
    lw $ra, 4($sp)
    addiu $sp, $sp, 8
    jr $ra
