.data
newline: .asciiz "\n"

.text
.globl main

    # function calcularCse

calcularCse:
    addiu $sp, $sp, -8
    sw $ra, 4($sp)
    sw $fp, 0($sp)
    move $fp, $sp
    move $s0, $a0
    move $s1, $a1

    # t0 = a + b
    move $t0, $s0
    move $t1, $s1
    add $v0, $t0, $t1
    move $t0, $v0

    # x = t0
    move $v0, $t0
    move $s2, $v0

    # t1 = t0
    move $v0, $t0
    move $t1, $v0

    # y = t1
    move $v0, $t1
    move $s3, $v0

    # t2 = y + 2
    move $t0, $s3
    li $t1, 2
    add $v0, $t0, $t1
    move $t2, $v0

    # z = t2
    move $v0, $t2
    move $s4, $v0

    # return z
    move $v0, $s4
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

    # param 5
    li $a0, 5

    # param 10
    li $a1, 10

    # t3 = call calcularCse, 2
    jal calcularCse
    move $t3, $v0

    # r = t3
    move $v0, $t3
    move $s5, $v0

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
