use loopdev::{LoopControl, LoopDevice};
use std::path::PathBuf;

mod util;
use crate::util::{
    attach_file, create_backing_file, detach_all, list_device, partition_backing_file, setup,
};

#[test]
fn get_next_free_device() {
    let num_devices_at_start = list_device(None).len();
    let _lock = setup();

    let lc = LoopControl::open().expect("should be able to open the LoopControl device");
    let ld0 = lc
        .next_free()
        .expect("should not error finding the next free loopback device");

    assert_eq!(
        ld0.path(),
        Some(PathBuf::from(&format!("/dev/loop{}", num_devices_at_start))),
        "should find the first loopback device"
    );
}

#[test]
fn attach_a_backing_file_default() {
    attach_a_backing_file(0, 0, 128 * 1024 * 1024);
}

#[test]
fn attach_a_backing_file_with_offset() {
    attach_a_backing_file(128 * 1024, 0, 128 * 1024 * 1024);
}

#[test]
fn attach_a_backing_file_with_sizelimit() {
    attach_a_backing_file(0, 128 * 1024, 128 * 1024 * 1024);
}

#[test]
fn attach_a_backing_file_with_offset_sizelimit() {
    attach_a_backing_file(128 * 1024, 128 * 1024, 128 * 1024 * 1024);
}

// This is also allowed by losetup, not sure what happens if you try to write to the file though.
#[test]
fn attach_a_backing_file_with_offset_overflow() {
    attach_a_backing_file(128 * 1024 * 1024 * 2, 0, 128 * 1024 * 1024);
}

// This is also allowed by losetup, not sure what happens if you try to write to the file though.
#[test]
fn attach_a_backing_file_with_sizelimit_overflow() {
    attach_a_backing_file(0, 128 * 1024 * 1024 * 2, 128 * 1024 * 1024);
}

fn attach_a_backing_file(offset: u64, sizelimit: u64, file_size: i64) {
    let _lock = setup();

    let (devices, ld0_path, file_path) = {
        let lc = LoopControl::open().expect("should be able to open the LoopControl device");

        let file = create_backing_file(file_size);
        let file_path = file.to_path_buf();
        let ld0 = lc
            .next_free()
            .expect("should not error finding the next free loopback device");

        ld0.with()
            .offset(offset)
            .size_limit(sizelimit)
            .attach(&file)
            .expect("should not error attaching the backing file to the loopdev");

        let devices = list_device(Some(ld0.path().unwrap().to_str().unwrap()));
        file.close().expect("should delete the temp backing file");

        (devices, ld0.path().unwrap(), file_path)
    };

    assert_eq!(
        devices.len(),
        1,
        "there should be only one loopback mounted device"
    );
    assert_eq!(
        devices[0].name.as_str(),
        ld0_path.to_str().unwrap(),
        "the attached devices name should match the input name"
    );
    assert_eq!(
        devices[0].back_file.clone().unwrap().as_str(),
        file_path.to_str().unwrap(),
        "the backing file should match the given file"
    );
    assert_eq!(
        devices[0].offset,
        Some(offset),
        "the offset should match the requested offset"
    );
    assert_eq!(
        devices[0].size_limit,
        Some(sizelimit),
        "the sizelimit should match the requested sizelimit"
    );

    detach_all();
}

#[test]
fn detach_a_backing_file_default() {
    detach_a_backing_file(0, 0, 128 * 1024 * 1024);
}

#[test]
fn detach_a_backing_file_with_offset() {
    detach_a_backing_file(128 * 1024, 0, 128 * 1024 * 1024);
}

#[test]
fn detach_a_backing_file_with_sizelimit() {
    detach_a_backing_file(0, 128 * 1024, 128 * 1024 * 1024);
}

#[test]
fn detach_a_backing_file_with_offset_sizelimit() {
    detach_a_backing_file(128 * 1024, 128 * 1024, 128 * 1024 * 1024);
}

// This is also allowed by losetup, not sure what happens if you try to write to the file though.
#[test]
fn detach_a_backing_file_with_offset_overflow() {
    detach_a_backing_file(128 * 1024 * 1024 * 2, 0, 128 * 1024 * 1024);
}

// This is also allowed by losetup, not sure what happens if you try to write to the file though.
#[test]
fn detach_a_backing_file_with_sizelimit_overflow() {
    detach_a_backing_file(0, 128 * 1024 * 1024 * 2, 128 * 1024 * 1024);
}

fn detach_a_backing_file(offset: u64, sizelimit: u64, file_size: i64) {
    let num_devices_at_start = list_device(None).len();
    let _lock = setup();

    {
        let file = create_backing_file(file_size);
        attach_file(
            "/dev/loop5",
            file.to_path_buf().to_str().unwrap(),
            offset,
            sizelimit,
        );

        let ld0 = LoopDevice::open("/dev/loop5")
            .expect("should be able to open the created loopback device");

        ld0.detach()
            .expect("should not error detaching the backing file from the loopdev");

        file.close().expect("should delete the temp backing file");
    };

    std::thread::sleep(std::time::Duration::from_millis(100));

    assert_eq!(
        list_device(None).len(),
        num_devices_at_start,
        "there should be no loopback devices mounted"
    );
    detach_all();
}

#[test]
fn attach_a_backing_file_with_part_scan_default() {
    attach_a_backing_file_with_part_scan(1 * 1024 * 1024);
}

fn attach_a_backing_file_with_part_scan(file_size: i64) {
    let _lock = setup();

    let partitions = {
        let lc = LoopControl::open().expect("should be able to open the LoopControl device");

        let file = create_backing_file(file_size);
        partition_backing_file(&file, 1024);

        let ld0 = lc
            .next_free()
            .expect("should not error finding the next free loopback device");

        ld0.with()
            .part_scan(true)
            .attach(&file)
            .expect("should not error attaching the backing file to the loopdev");
        let devices = list_device(Some(ld0.path().unwrap().to_str().unwrap()));
        let partitions = glob::glob(&format!("{}p*", devices[0].name))
            .unwrap()
            .map(|entry| entry.unwrap().display().to_string())
            .collect::<Vec<_>>();

        file.close().expect("should delete the temp backing file");

        partitions
    };

    assert_eq!(
        partitions.len(),
        1,
        "there should be only one partition for the device"
    );
}

#[test]
fn add_a_loop_device() {
    let _lock = setup();
    let dev = std::fs::read_dir("/dev").expect("should be able to open /dev");
    let device_count = dev
        .map(|r| {
            r.as_ref()
                .expect("readdir failed")
                .file_name()
                .to_string_lossy()
                .to_string()
        })
        .filter(|r| r != "loop-control")
        .filter(|r| r.contains("loop"))
        .count();

    let lc = LoopControl::open().expect("should be able to open the LoopControl device");
    assert!(lc.add(device_count as u32).is_err()); // EEXIST
    assert!(lc.add(device_count as u32 + 1).is_ok())
}
