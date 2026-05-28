.data
newline: .asciiz "\n"

.text
.globl main

    # function main

main:
    addiu $sp, $sp, -16
    sw $ra, 4($sp)
    sw $fp, 0($sp)
    move $fp, $sp

    # a = 5
    li $v0, 5
    sw $v0, -4($fp)

    # t0 = &a
    lw $t0, -4($fp)
    addiu $v0, $fp, -4
    move $t0, $v0

    # p = t0
    move $v0, $t0
    move $s0, $v0

    # *p = 10
    li $v0, 10
    move $t8, $s0
    sw $v0, 0($t8)

    # b = a
    lw $v0, -4($fp)
    move $s1, $v0

    # return b
    move $v0, $s1
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
