.data
newline: .asciiz "\n"

.text
.globl main

    # function main

main:
    addiu $sp, $sp, -8
    sw $ra, 4($sp)
    sw $fp, 0($sp)
    move $fp, $sp

    # a = 5
    li $v0, 5
    move $s0, $v0

    # b = 10
    li $v0, 10
    move $s1, $v0

    # c = 15
    li $v0, 15
    move $s2, $v0

    # d = 15
    li $v0, 15
    move $s3, $v0

    # e = 15
    li $v0, 15
    move $s4, $v0

    # f = 30
    li $v0, 30
    move $s5, $v0

    # return 30
    li $v0, 30
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
