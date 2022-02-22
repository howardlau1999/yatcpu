#ifndef _COMPLIANCE_MODEL_H_
#define _COMPLIANCE_MODEL_H_

#define ALIGNMENT 2

#define RVMODEL_DATA_SECTION \
    .pushsection .tohost,"aw",@progbits; \
    .align 4; .global tohost; tohost: .word 0; \
    .popsection;

#define RVMODEL_BOOT

#define RVMODEL_HALT \
    li x1, 0xBABECAFE; \
    write_tohost: \
        sw x1, tohost, x0; \
    loop: j loop

//RV_COMPLIANCE_DATA_BEGIN
#define RVMODEL_DATA_BEGIN                                              \
  .align 4; .global begin_signature; begin_signature:

//RV_COMPLIANCE_DATA_END
#define RVMODEL_DATA_END                                                      \
  .align 4; .global end_signature; end_signature:  \
  RVMODEL_DATA_SECTION                                                        \

#endif 