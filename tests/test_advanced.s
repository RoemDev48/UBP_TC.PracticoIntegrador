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

    # flag = true
    li $v0, 1
    move $s1, $v0

    # flag2 = false
    li $v0, 0
    move $s2, $v0

    # i = 0
    li $v0, 0
    move $s3, $v0

    # L0:
L0:

    # t0 = i < 5
    move $t0, $s3
    li $t1, 5
    slt $v0, $t0, $t1
    move $t0, $v0

    # ifFalse t0 goto L1
    beqz $t0, L1

    # t1 = i * 10
    move $t0, $s3
    li $t1, 10
    mul $v0, $t0, $t1
    move $t1, $v0

    # datos[i] = t1
    move $v0, $t1
    move $s0, $v0

    # t2 = i + 1
    move $t0, $s3
    li $t1, 1
    add $v0, $t0, $t1
    move $t2, $v0

    # i = t2
    move $v0, $t2
    move $s3, $v0

    # goto L0
    j L0

    # L1:
L1:

    # x = 100
    li $v0, 100
    sw $v0, -4($fp)

    # t3 = &x
    lw $t0, -4($fp)
    addiu $v0, $fp, -4
    move $t3, $v0

    # ptr = t3
    move $v0, $t3
    move $s4, $v0

    # *ptr = 50
    li $v0, 50
    move $t8, $s4
    sw $v0, 0($t8)

    # t4 = datos[2]
    move $v0, $s0
    move $t4, $v0

    # valor = t4
    move $v0, $t4
    move $s5, $v0

    # t5 = x + valor
    lw $t0, -4($fp)
    move $t1, $s5
    add $v0, $t0, $t1
    move $t5, $v0

    # resultado = t5
    move $v0, $t5
    move $s6, $v0

    # return resultado
    move $v0, $s6
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
