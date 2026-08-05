-- Add the privacy-compute category to MPC program metadata.
-- This migration contains schema and built-in MPC metadata only; no user data.
ALTER TABLE `mpc`
    ADD COLUMN `task_type` ENUM(
        'GARNET_MPC',
        'GARNET_PSI',
        'GARNET_INFERENCE'
    ) NULL DEFAULT NULL AFTER `name`;

UPDATE `mpc`
SET `task_type` = 'GARNET_MPC'
WHERE `uid` = 'correction-supervision';

UPDATE `mpc`
SET `task_type` = 'GARNET_PSI'
WHERE `uid` = 'PSI_GARNET';

UPDATE `mpc`
SET `task_type` = 'GARNET_INFERENCE'
WHERE `uid` = 'davex-dt-inference';
